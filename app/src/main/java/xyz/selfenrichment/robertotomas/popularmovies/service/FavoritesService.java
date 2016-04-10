package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0009, 9, 4, 2016.

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.selfenrichment.robertotomas.popularmovies.SQLite.FavoritesContract;
import xyz.selfenrichment.robertotomas.popularmovies.SQLite.FavoritesHelper;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;

/**
 * Class which provides movies for the favorites view
 */
public class FavoritesService extends IntentService {
    private final String LOG_TAG = MoviesService.class.getSimpleName();

    private static final int LOADER_ID_NETWORK = 123321;
    private CursorLoader mCursorLoader;

    public FavoritesService() {
        super("FavoritesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FavoritesHelper dbhelper = new FavoritesHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
/*        Cursor cursor = db.rawQuery("SELECT ?,?,?,?, ?,?,?,? FROM favorites ORDER BY _id DESC",
                new String[] { FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY,
                        FavoritesContract.FavoritesEntry.COL_TITLE,
                        FavoritesContract.FavoritesEntry.COL_RELEASE_DATE,
                        FavoritesContract.FavoritesEntry.COL_VOTE_AVERAGE,
                        FavoritesContract.FavoritesEntry.COL_POSTER_PATH,
                        FavoritesContract.FavoritesEntry.COL_POSTER_TYPE,
                        FavoritesContract.FavoritesEntry.COL_BACKDROP_PATH,
                        FavoritesContract.FavoritesEntry.COL_OVERVIEW }
                ); */
        String columns = FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + ", " +
                FavoritesContract.FavoritesEntry.COL_TITLE + ", " +
                FavoritesContract.FavoritesEntry.COL_RELEASE_DATE + ", " +
                FavoritesContract.FavoritesEntry.COL_VOTE_AVERAGE + ", " +
                FavoritesContract.FavoritesEntry.COL_POSTER_PATH + ", " +
                FavoritesContract.FavoritesEntry.COL_POSTER_TYPE + ", " +
                FavoritesContract.FavoritesEntry.COL_BACKDROP_PATH + ", " +
                FavoritesContract.FavoritesEntry.COL_OVERVIEW;
        Cursor cursor = db.rawQuery("SELECT "+columns+" FROM favorites ORDER BY _id DESC", null);
      /*  c.moveToFirst();
        int i = c.getInt(0);
        Cursor cursor = db.query(
            FavoritesContract.FavoritesEntry.TABLE_NAME,
            new String[] {FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY,
                FavoritesContract.FavoritesEntry.COL_TITLE,
                FavoritesContract.FavoritesEntry.COL_RELEASE_DATE,
                FavoritesContract.FavoritesEntry.COL_VOTE_AVERAGE,
                FavoritesContract.FavoritesEntry.COL_POSTER_PATH,
                FavoritesContract.FavoritesEntry.COL_POSTER_TYPE,
                FavoritesContract.FavoritesEntry.COL_BACKDROP_PATH,
                FavoritesContract.FavoritesEntry.COL_OVERVIEW },
            null,
            null,
            null,
            null,
            FavoritesContract.FavoritesEntry.COL_ID + " DESC");*/

        List<Movie> movies = new ArrayList<>();
        try {
            if (!(cursor.moveToFirst()) || (cursor.getCount() == 0)){
                // TODO - handle empty favorites list
                // notify user to mark some movies as favorites.
                // redirect user from there to the main listing
            } else {
                //Log.d(LOG_TAG, "#results: "+Integer.toString(cursor.getCount())+" ,movie with tmbd_id: " + cursor.getString(0) + " and title of: " + cursor.getString(1));
                do {
                    Movie movie = new Movie();
                    movie.setId(cursor.getString(0));
                    movie.setTitle(cursor.getString(1));
                    movie.setRelease_date(cursor.getString(2));
                    movie.setVote_average(cursor.getString(3));
                    movie.setPoster_path(cursor.getString(4));
                    movie.setPoster_type(cursor.getString(5));
                    movie.setBackdrop_path(cursor.getString(6));
                    movie.setOverview(cursor.getString(7));
                    movies.add(movie);
                    Log.d(LOG_TAG, "movie found: title: " + movie.getTitle());
                }while(cursor.moveToNext());
            }
        } catch(Exception e) {
            Log.w(this.getClass().getSimpleName(), "onLoadFinished (Favorites) Error: " +
                    e.getMessage() );
        } finally {
            cursor.close();
            db.close();
        }
        onPostExecute(movies.toArray(new Movie[]{}));
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(Movie[] movies) {
        Intent intent = new Intent(FavoritesService.class.getSimpleName());
        intent.putExtra(AbstractService.HANDLER_RESULT, movies);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

