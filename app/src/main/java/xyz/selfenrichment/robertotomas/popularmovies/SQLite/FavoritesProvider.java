package xyz.selfenrichment.robertotomas.popularmovies.SQLite;
// Created by RobertoTomás on 0002, 2, 4, 2016.

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * The favorites provider class define the uri-specific aspects of favorites.
 */
public class FavoritesProvider extends ContentProvider {
    private static final String LOG_TAG = FavoritesProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesHelper mOpenHelper;

    // Codes for the UriMatcher //////
    private static final int QUERY_RESULTS_LIST = 100;
    private static final int QUERY_RESULT_ITEM = 200;
    ////////

    private static UriMatcher buildUriMatcher(){
        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.CONTENT_AUTHORITY;

        // add a code for each type of URI you want
        matcher.addURI(authority, FavoritesContract.FavoritesEntry.TABLE_NAME, QUERY_RESULTS_LIST);
        matcher.addURI(authority, FavoritesContract.FavoritesEntry.TABLE_NAME + "/#",
                QUERY_RESULT_ITEM);
        matcher.addURI(authority, FavoritesContract.FavoritesEntry.TABLE_NAME + "/"+
                    FavoritesContract.FavoritesEntry.COL_TITLE +"/*",
                QUERY_RESULTS_LIST);

        return matcher;
    }

    @Override
    public boolean onCreate(){
        mOpenHelper = new FavoritesHelper(getContext());

        return true;
    }

    @Override
    public String getType(@NonNull Uri uri){
        final int match = sUriMatcher.match(uri);

        switch (match){
            case QUERY_RESULTS_LIST: {
                return FavoritesContract.FavoritesEntry.ContentUris.CONTENT_TYPE;
            }
            case QUERY_RESULT_ITEM:{
                return FavoritesContract.FavoritesEntry.ContentUris.CONTENT_ITEM_TYPE;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder){
     /*   Log.d(LOG_TAG, "query URI: "+uri+
            "projection: "+((projection==null)?"null":projection.toString())+
            " selection: "+selection+
            " selectionargs: "+((selectionArgs==null)?"null":selectionArgs.toString())+
            " sortOrder: "+sortOrder); */

        Cursor retCursor;
        switch(sUriMatcher.match(uri)){
            // All Favorites selected
            // All Flavors selected
            case QUERY_RESULTS_LIST:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            // Individual Favorite based on Id selected
            case QUERY_RESULT_ITEM:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            default:{
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    @Nullable
    public Uri insert(@NonNull Uri uri, ContentValues values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case QUERY_RESULTS_LIST: {
                long _id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = FavoritesContract.FavoritesEntry.ContractEntry.buildTypeUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into: " + uri);
                }
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);

            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match){
            case QUERY_RESULTS_LIST:
                numDeleted = db.delete(
                        FavoritesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        FavoritesContract.FavoritesEntry.TABLE_NAME + "'");
                break;
            case QUERY_RESULT_ITEM:
                numDeleted = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        FavoritesContract.FavoritesEntry.TABLE_NAME + "'");

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(numDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match){
            case QUERY_RESULTS_LIST:
                // allows for multiple transactions
                db.beginTransaction();

                // keep track of successful inserts
                int numInserted = 0;
                try{
                    for(ContentValues value : values){
                        if (value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try{
                            _id = db.insertOrThrow(FavoritesContract.FavoritesEntry.TABLE_NAME,
                                    null, value);
                        }catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            FavoritesContract.FavoritesEntry.COL_TITLE)
                                    + " but value is already in database.");
                        }
                        if (_id != -1){
                            numInserted++;
                        }
                    }
                    if(numInserted > 0){
                        // If no errors, declare a successful transaction.
                        // database will not populate if this is not called
                        db.setTransactionSuccessful();
                    }
                } finally {
                    // all transactions occur at once
                    db.endTransaction();
                }
                if (numInserted > 0){
                    // if there was successful insertion, notify the content resolver that there
                    // was a change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated = 0;

        if (contentValues == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case QUERY_RESULTS_LIST:{
                numUpdated = db.update(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            }
            case QUERY_RESULT_ITEM: {
                numUpdated = db.update(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        contentValues,
                        FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0){
            Log.d(LOG_TAG, "...and we did something");
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }

}