package xyz.selfenrichment.robertotomas.popularmovies.SQLite;
// Created by RobertoTomás on 0031, 31, 3, 2016.

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;

/**
 * Because the Provider itself should not have anything specific to the database in it, we provide a
 * helper class to sheppard the few SQL calls it might otherwise normally use. A further refactoring
 * would move these into the contract parent class.
 */
public class FavoritesHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 11;
    static final String DB_NAME = "favorites.db";

    public  FavoritesHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(this.getClass().getSimpleName(), "- in onCreate");
        for (String table : FavoritesContract.TABLES) {
            Log.d(this.getClass().getSimpleName(), "creating table: " + table);
            Log.d(this.getClass().getSimpleName() + "SCHEMA", FavoritesContract.getSchema(table));
            db.execSQL("CREATE TABLE " + FavoritesContract.getSchema(table));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(this.getClass().getSimpleName(), "in onUpgrade");
        for(String table : FavoritesContract.TABLES){
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }

    public static ContentValues serializeMovie(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY, movie.getId());
        values.put(FavoritesContract.FavoritesEntry.COL_TITLE, movie.getTitle());
        values.put(FavoritesContract.FavoritesEntry.COL_RELEASE_DATE, movie.getRelease_date());
        values.put(FavoritesContract.FavoritesEntry.COL_VOTE_AVERAGE, movie.getVote_average());
        values.put(FavoritesContract.FavoritesEntry.COL_POSTER_PATH, movie.getPoster_path());
        values.put(FavoritesContract.FavoritesEntry.COL_POSTER_TYPE, movie.getPoster_type());
        values.put(FavoritesContract.FavoritesEntry.COL_BACKDROP_PATH, movie.getBackdrop_path());
        values.put(FavoritesContract.FavoritesEntry.COL_OVERVIEW, movie.getOverview());
        return values;
    }
}
