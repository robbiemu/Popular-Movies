package xyz.selfenrichment.robertotomas.popularmovies.SQLite;
// Created by RobertoTomás on 0031, 31, 3, 2016.

import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * The favorites contract class (and its associated helper class) define the database-specific
 * aspects of the favorites table.
 */
public class FavoritesContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "xyz.selfenrichment.robertotomas.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_FAVORITES = "favorites";

    public static String[] TABLES = new String[]{ FavoritesEntry.ContractEntry.TABLE_NAME };
    public static Map<String, Class> TABLES_MAP = new HashMap<>();
    static {
        TABLES_MAP.put(FavoritesEntry.ContractEntry.TABLE_NAME, FavoritesEntry.class);
    }

    public static String getSchema(String table) {
        if (table.equals(FavoritesEntry.ContractEntry.TABLE_NAME)) {
            return FavoritesEntry.ContractEntry.TABLE_SCHEMA;
        } else {
            //default case
            Log.e(FavoritesContract.class.getSimpleName(), "Requested schema of non-existing table '"+table+"'");

            return null;
        }
    }

    public static String[] getColumns(String table) {
        if (table.equals(FavoritesEntry.ContractEntry.TABLE_NAME)) {
            return FavoritesEntry.ContractEntry.COLUMNS.getNames();
        } else {
            //default case
            Log.e(FavoritesContract.class.getSimpleName(), "Requested column names of non-existing table '"+table+"'");

            return null;
        }
    }

    public static Map<String, String> getColumnTypeMappings(String table) {
        if (table.equals(FavoritesEntry.ContractEntry.TABLE_NAME)) {
            return FavoritesEntry.ContractEntry.COLUMNS.COLUMN_NAME_TYPE_MAP;
        } else {
            //default case
            Log.e(FavoritesContract.class.getSimpleName(), "Requested column type mapping of non-existing table '"+table+"'");

            return null;
        }
    }

    public static class FavoritesEntry {
        public static ContractEntryWithAPI ContractEntry = new ContractEntryWithAPI("favorites","movies");
        public static final String COL_TITLE = "title";
        public static final String COL_THEMOVIEDBKEY = "id";
        public static final String COL_ID = "_id"; // primary key
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_VOTE_AVERAGE = "vote_average";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_POSTER_TYPE = "poster_type";
        public static final String COL_BACKDROP_PATH = "backdrop_path";
        public static final String COL_OVERVIEW = "overview";
        static{
            ContractEntry.COLUMNS.putColumn(COL_ID, "INTEGER");
            ContractEntry.COLUMNS.putColumn(COL_RELEASE_DATE, "VARCHAR(64)");
            ContractEntry.COLUMNS.putColumn(COL_THEMOVIEDBKEY, "VARCHAR(64)");
            ContractEntry.COLUMNS.putColumn(COL_TITLE, "VARCHAR(255)");
            ContractEntry.COLUMNS.putColumn(COL_VOTE_AVERAGE, "VARCHAR(64)");
            ContractEntry.COLUMNS.putColumn(COL_POSTER_PATH, "VARCHAR(255)");
            ContractEntry.COLUMNS.putColumn(COL_POSTER_TYPE, "VARCHAR(64)");
            ContractEntry.COLUMNS.putColumn(COL_BACKDROP_PATH, "VARCHAR(255)");
            ContractEntry.COLUMNS.putColumn(COL_OVERVIEW, "TEXT");
            Map<String, String> columnModifiers = new HashMap<>();
//            columnModifiers.put(COL_TITLE, "NOT NULL");
            columnModifiers.put(COL_THEMOVIEDBKEY, "NOT NULL");
            columnModifiers.put(COL_ID, "PRIMARY KEY AUTOINCREMENT");
            String tableModifiers =
                    // ensure utf-8 encoding
                    " PRAGMA ENCODING 'utf8', " +
                    // To assure the application have just one weather entry per day
                    // per location, it's created a UNIQUE constraint with REPLACE strategy
                    " UNIQUE ( "+ COL_TITLE +", "+ COL_THEMOVIEDBKEY +") ON CONFLICT REPLACE";
            try {
                ContractEntry.buildAndSaveSchema(columnModifiers, tableModifiers);
            }catch (Exception e) {
                Log.e("FavoritesContract", "error with building schema in Favorites Entry. Error: " + e.getMessage());
            }
        }
        public static final String TABLE_NAME = ContractEntry.TABLE_NAME;
        public static final String TABLE_SCHEMA = ContractEntry.TABLE_SCHEMA;
        public static final ContractEntryWithAPI.Columns COLUMNS = ContractEntry.COLUMNS;
        public static ContractEntryWithAPI.ContentUri ContentUris = ContractEntry.CONTENT_URI;

        public static Uri buildFavorite(String id) {
            return ContractEntry.CONTENT_URI.CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildFavoriteWithName(String testName) {
            return ContractEntry.CONTENT_URI.CONTENT_URI.buildUpon().appendPath("name")
                    .appendPath(testName).build();
        }
    }

}
