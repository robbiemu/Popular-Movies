package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Get Genres for a movie
 */
public class GenresService extends AbstractService {
    public static String INTENTEXTRA_ID = "id";
    private final String LOG_TAG = GenresService.class.getSimpleName();

    public GenresService() {
        super("GenresService");
    }

    String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//            Log.d(LOG_TAG, "Starting GetGenre API transaction");

        String id = intent.getStringExtra(INTENTEXTRA_ID);
        if (id == null) return;

        String resultsJSON = getDetailsJSONFromAPI(id);
        try {
            onPostExecute(getGenresOfMoviesFromJSON(resultsJSON));
        } catch (JSONException e) {
            Log.e(getLogTag(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String getGenresOfMoviesFromJSON(String json_str) throws JSONException {
        JSONObject movieGenresJson = new JSONObject(json_str);
        JSONArray movieGenresArray = movieGenresJson.getJSONArray("genres");

        List<String> resultGenres = new ArrayList<>();
        for (int i = 0; i < movieGenresArray.length(); i++) {
            JSONObject genre = movieGenresArray.getJSONObject(i);
//            String id = genre.getString("id");
            String name = genre.getString("name");

            resultGenres.add(name);
//            Log.v(LOG_TAG, String.format("Movie genre found: id: %s, name: %s", id, name);
        }

        StringBuilder builder = new StringBuilder();
        Boolean firstpass = true;
        for (String value : resultGenres) {
            builder.append(firstpass ? value : ", " + value);
            firstpass = false;
        }
        String genres = builder.toString();

        return genres;
    }

    protected void onPostExecute(String genres) {
        Intent intent = new Intent(GenresService.class.getSimpleName());
        intent.putExtra(HANDLER_RESULT, genres);
        intent.putExtra(INTENT_TYPE, GenresService.class.getSimpleName());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
