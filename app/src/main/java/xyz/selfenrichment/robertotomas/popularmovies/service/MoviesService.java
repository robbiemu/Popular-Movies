package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import xyz.selfenrichment.robertotomas.popularmovies.BuildConfig;
import xyz.selfenrichment.robertotomas.popularmovies.R;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;
import xyz.selfenrichment.robertotomas.popularmovies.lib.PosterSVG;

/**
 * run the discover/movies query
 */
public class MoviesService extends AbstractService {
    private final String LOG_TAG = MoviesService.class.getSimpleName();

    private static final String SUBTAG_JSON = " json:";
    private static final String SUBTAG_LASTRUN = " lastrun:";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    public MoviesService() {
        super("MoviesService");
    }

    String getLogTag(){
        return LOG_TAG;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String resultsJSON = (isTimeForSync()?getMovies():getLastSync());
        try {
            onPostExecute(getMoviesFromJSON(
                    resultsJSON,
                    getResources().getInteger(R.integer.pref_tmdb_results_num_movies)
            ));
        } catch (JSONException e) {
            Log.e(getLogTag(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String getLastSync() {
        return getServiceRecord().get(SUBTAG_JSON + getSortByTag(this));
    }

    private boolean isTimeForSync() {
        String lastrun = getServiceRecord().get(SUBTAG_LASTRUN + getSortByTag(this));
        if(lastrun == null){
            lastrun = "0";
        }
        return System.currentTimeMillis() > Long.parseLong(lastrun) + DAY_IN_MILLIS;
    }

    @Nullable
    private HashMap<String, String> getServiceRecord() {
        String jsonString = Prefs.getString(LOG_TAG, "{}");
        Type type = new TypeToken<HashMap<String,String>>(){}.getType();
        return new Gson().fromJson(jsonString, type); // Hashmap<String, String>
    }

    private void setServiceRecord(HashMap<String, String> serviceRecord) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
        Type type = new TypeToken<HashMap<String,String>>(){}.getType();
        String json = gson.toJson(serviceRecord, type);
        Prefs.putString(LOG_TAG, json);
    }

    @NonNull
    public static String getSortByTag(Context c) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(c);
        return SP.getString("pref_tmdb_bp_query_sort_by",
                c.getString(R.string.pref_tmdb_defaults_sort_by)) +
                "." +
                (SP.getBoolean("pref_tmdb_bp_query_sort_dir", c.getResources().getBoolean(R.bool.pref_tmdb_defaults_sort_dir))?
                        "desc":
                        "asc");
    }

    /**
     * getMoviesFromJSON
     * @param json_str - the json string from the web api
     * @param num_movies - number of results to list
     * @return returns an array of movie objects from the json data
     * @throws JSONException
     * composites the results from the JSON string into a String[][] array to be returned from this AsyncTask.
     */
    private Movie[] getMoviesFromJSON(String json_str, int num_movies) throws JSONException {
        JSONObject moviesJson = new JSONObject(json_str);
        JSONArray moviesArray = moviesJson.getJSONArray("results");

        Movie[] resultMovies = new Movie[num_movies];
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieJSON = moviesArray.getJSONObject(i);
            Map<String,String> mapMovie = new HashMap<>();
            mapMovie.put("id", movieJSON.getString("id"));
            mapMovie.put("poster_path", movieJSON.getString("poster_path"));
            mapMovie.put("poster_type", null);
            mapMovie.put("title", movieJSON.getString("title"));
            mapMovie.put("vote_average", movieJSON.getString("vote_average"));
            mapMovie.put("backdrop_path", movieJSON.getString("backdrop_path"));
            mapMovie.put("overview", movieJSON.getString("overview"));
            mapMovie.put("release_date", movieJSON.getString("release_date"));

            Movie movie = new Movie(mapMovie);

            if (movieJSON.getString("poster_path").equals("null")) {
                PosterSVG psvg;
                if (!(movieJSON.getString("title").equals("null"))) {
                    psvg = new PosterSVG( TextUtils.htmlEncode(movieJSON.getString("title")) );
                }else{
                    Log.e(LOG_TAG,"Movie with no title and no poster in results from themoviedb");
                    psvg = new PosterSVG("themoviedb error");
                }


                Prefs.putString(movieJSON.getString("id"), psvg.toString());
                movie.setPoster_path(movieJSON.getString("id"));
                movie.setPoster_type("local-svg");
            }else{
                movie.setPoster_type("tmdb");
            }

            resultMovies[i] = movie;
//            Log.d(LOG_TAG, String.format("id: %s, poster_path: %s, title: %s, avg.vote: %s, backdrop_path: %s, and %s overview",
//                    movieJSON.getString("id"),
//                    movie.getPoster_type() + "://" + movieJSON.getString("poster_path"),
//                    movieJSON.getString("title"),
//                    movieJSON.getString("vote_average"),
//                    movieJSON.getString("backdrop_path"),
//                    ((movieJSON.getString("overview")=="null")?"no":"an") ));

        }
        return resultMovies;
    }

    /**
     * getMovies
     * @return
     * Handles the request to themoviedb and returns the JSON string.
     */
    private String getMovies(){
        // I'm using `pref_text_tmdb_sort_dir_asc` instead of hard coding it (ie, "asc") like the
        // other parameters because the value once set comes from these fields.
        // We should only define the values in one place.
        String sort_by = getSortByTag(this);

    /*Log.d(LOG_TAG, "Sort dir default: " +
             mContext.getString(R.bool.pref_tmdb_defaults_sort_dir) + " => " +
            (mContext.getResources().getBoolean(R.bool.pref_tmdb_defaults_sort_dir)?
                    "desc":
                    "asc")
    );*/

        Uri builtUri = Uri.parse(getString(R.string.tmdb_bp_BASE_URL)).buildUpon()
                .appendQueryParameter("api_key", BuildConfig.API_KEY_THEMOVIEDB)
                .appendQueryParameter("sort_by", sort_by)
                .build();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
//        HttpURLConnection urlConnection = null;
        //BufferedReader reader = null;

        String returnString = getJSONStringFromAPI(builtUri, null);

    /*int maxLogSize = 1000;
    for(int i = 0; i <= returnString.length() / maxLogSize; i++) {
        int start = i * maxLogSize;
        int end = (i+1) * maxLogSize;
        end = end > returnString.length() ? returnString.length() : end;
        Log.v(LOG_TAG, returnString.substring(start, end));
    }*/

        HashMap<String,String> serviceRecord = getServiceRecord();
        serviceRecord.put(SUBTAG_LASTRUN + sort_by,
                Long.toString(System.currentTimeMillis()));
        serviceRecord.put(SUBTAG_JSON + sort_by, returnString);
        setServiceRecord(serviceRecord);

        return returnString;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(Movie[] movies) {
        Intent intent = new Intent(MoviesService.class.getSimpleName());
        intent.putExtra(HANDLER_RESULT, movies);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
