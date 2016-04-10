package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import xyz.selfenrichment.robertotomas.popularmovies.R;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Review;

/**
 * Get reviews for a movie
 */
public class ReviewsService extends AbstractService {
    public static String INTENTEXTRA_ID = "id";
    private final String LOG_TAG = ReviewsService.class.getSimpleName();
    private String mIDofMovie = null;

    public ReviewsService() { super("ReviewsService"); }

    String getLogTag() {
        return LOG_TAG;
    }

    protected void onHandleIntent(Intent intent) {
//            Log.d(LOG_TAG, "Starting GetReviews API transaction");

        mIDofMovie = intent.getStringExtra(INTENTEXTRA_ID);
        if(mIDofMovie == null) return;

        String resultsJSON = getDetailsJSONFromAPI(mIDofMovie, getString(R.string.tmdb_reviews_path));
        try {
            onPostExecute(getReviewsOfMoviesFromJSON(resultsJSON));
        } catch (JSONException e) {
            Log.e(getLogTag(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Review[] getReviewsOfMoviesFromJSON(String json_str) throws JSONException {
        JSONObject movieReviewsJson = new JSONObject(json_str);
        JSONArray movieReviewsArray = movieReviewsJson.getJSONArray("results");


        Review[] resultReviews = new Review[movieReviewsArray.length()];
        for (int i = 0; i < movieReviewsArray.length(); i++) {
            JSONObject reviewJSON = movieReviewsArray.getJSONObject(i);
            Map<String,String> mapReview = new HashMap<>();

            mapReview.put("id_of_movie", mIDofMovie);
            mapReview.put("id", reviewJSON.getString("id"));
            mapReview.put("author", reviewJSON.getString("author"));
            mapReview.put("content", reviewJSON.getString("content"));
            mapReview.put("url", reviewJSON.getString("url"));

            Review review = new Review(mapReview);
            resultReviews[i] = review;
        }
        return resultReviews;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(Review[] reviews) {
        Intent intent = new Intent(ReviewsService.class.getSimpleName());
        intent.putExtra(HANDLER_RESULT, reviews);
        intent.putExtra(INTENT_TYPE, ReviewsService.class.getSimpleName());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
