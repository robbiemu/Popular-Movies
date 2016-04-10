package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.selfenrichment.robertotomas.popularmovies.R;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Video;

/**
 * Get trailers for a movie (links to them)
 */
public class TrailersService extends AbstractService {
    private final String LOG_TAG = TrailersService.class.getSimpleName();

    public static final String OFFICIAL = "official";
    public static final String TRAILER = "trailer";
    public static final String[] SECTIONS = {"youtube", "quicktime"};

    public static String INTENTEXTRA_ID = "id";

    private String mIDofMovie = null;

    public TrailersService() {
        super("TrailersService");
    }

    String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//            Log.d(LOG_TAG, "Starting GetTrailers API transaction");

        mIDofMovie = intent.getStringExtra(INTENTEXTRA_ID);
        if (mIDofMovie == null) return;
        String resultsJSON = getDetailsJSONFromAPI(mIDofMovie, getString(R.string.tmdb_trailers_path));

        try {
            onPostExecute(getTrailersOfMoviesFromJSON(resultsJSON));
        } catch (JSONException e) {
            Log.e(getLogTag(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Video[] getTrailersOfMoviesFromJSON(String json_str) throws JSONException {
        JSONObject movieTrailersJson = new JSONObject(json_str);

        Map<String, Map<String, List<Video>>> results = new HashMap<String, Map<String, List<Video>>>();
        results.put(SECTIONS[0], getVideosFromJsonSegment(SECTIONS[0], movieTrailersJson));
        results.put(SECTIONS[1], getVideosFromJsonSegment(SECTIONS[1], movieTrailersJson));

        /** We want the following order in results:
         *   1. All official trailers from source with most total trailers, preferably youtube.
         *   2. All official videos from both sources
         *   3. All other official trailers
         *   4. All other trailers
         *   5. All other videos
         */
        List<Video> resultVideosList = new ArrayList<Video>();
        // *   1. All official trailers from source with most total trailers, preferably youtube.
        int dim = (( results.get(SECTIONS[0]).get("official trailers").size() >=
                     results.get(SECTIONS[1]).get("official trailers").size() ) ?
                   0 : 1);
        for(Video v: results.get(SECTIONS[dim]).get("official trailers")) {
            resultVideosList.add(v);
        }

        // *   2. All official videos from both sources
        for(Video v: results.get(SECTIONS[0]).get("official videos")) {
            resultVideosList.add(v);
        }
        for(Video v: results.get(SECTIONS[1]).get("official videos")) {
            resultVideosList.add(v);
        }

        // *   3. All other official trailers
        for(Video v: results.get(SECTIONS[(dim == 0)?1:0]).get("official trailers")) {
            resultVideosList.add(v);
        }

        // *   4. All other trailers
        for(Video v: results.get(SECTIONS[0]).get("trailers")) {
            resultVideosList.add(v);
        }
        for(Video v: results.get(SECTIONS[1]).get("trailers")) {
            resultVideosList.add(v);
        }

        // *   5. All other videos
        for(Video v: results.get(SECTIONS[0]).get("videos")) {
            resultVideosList.add(v);
        }
        for(Video v: results.get(SECTIONS[1]).get("videos")) {
            resultVideosList.add(v);
        }

        Video[] videos = new Video[resultVideosList.size()];
        resultVideosList.toArray(videos);
        return videos;
    }

    @NonNull
    private Map<String, List<Video>> getVideosMap() {
        List<Video> resultTrailers = new ArrayList<Video>();
        List<Video> resultOfficialVideos = new ArrayList<Video>();
        List<Video> resultOfficialTrailers = new ArrayList<Video>();
        List<Video> resultVideos = new ArrayList<Video>();
        Map<String, List<Video>> results = new HashMap<String, List<Video>>();
        results.put("videos", resultVideos);
        results.put("trailers", resultTrailers);
        results.put("official videos", resultOfficialVideos);
        results.put("official trailers", resultOfficialTrailers);
        return results;
    }

    private Map<String, List<Video>> getVideosFromJsonSegment(String section, JSONObject movieTrailersJson) throws JSONException {
        Map<String, List<Video>> results = getVideosMap();

        JSONArray movieTrailersArray = movieTrailersJson.getJSONArray(section);
        for (int i = 0; i < movieTrailersArray.length(); i++) {
            JSONObject reviewJSON = movieTrailersArray.getJSONObject(i);
            Map<String,String> mapTrailers = new HashMap<>();

            try {
                mapTrailers.put("id_of_movie", mIDofMovie);
                mapTrailers.put("source", reviewJSON.getString("source"));
                mapTrailers.put("name", reviewJSON.getString("name"));
                mapTrailers.put("size", reviewJSON.getString("size"));
                mapTrailers.put("type", reviewJSON.getString("type"));
            }catch (Exception e) {
                Log.v(LOG_TAG, "JSON reply was: " + reviewJSON);
                throw e;
            }

            Video video = new Video(mapTrailers);
            if(video.getName().toLowerCase().contains(OFFICIAL)){
                if(video.getType().toLowerCase().contains(TRAILER)) {
                    results.get("official trailers").add(video);
                }
                results.get("official videos").add(video);
            } else if (video.getType().toLowerCase().contains(TRAILER)) {
                results.get("trailers").add(video);
            } else {
                results.get("videos").add(video);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(Video[] videos) {
        Intent intent = new Intent(TrailersService.class.getSimpleName());
        intent.putExtra(HANDLER_RESULT, videos);
        intent.putExtra(INTENT_TYPE, TrailersService.class.getSimpleName());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
