package xyz.selfenrichment.robertotomas.popularmovies.service;
// Created by RobertoTomás on 0031, 31, 3, 2016.

import android.app.IntentService;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import xyz.selfenrichment.robertotomas.popularmovies.BuildConfig;
import xyz.selfenrichment.robertotomas.popularmovies.R;

/**
 * Common methods for our service classes
 */
public abstract class AbstractService extends IntentService {
    public static String INTENTEXTRA_ID=null;
    public static String INTENT_TYPE="intent type";
    public static String HANDLER_RESULT = "result";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractService(String name) {
        super(name);
    }

    abstract String getLogTag();

    @Nullable
    protected String getJSONStringFromAPI(Uri builtUri, HttpURLConnection urlConnection) {
        String returnString;
        BufferedReader reader = null;
        try {
            URL url = new URL(builtUri.toString());

            Log.d(this.getClass().getSimpleName(), builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            returnString = buffer.toString();
        }catch(IOException e){
            Log.e(getLogTag(), "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(getLogTag(), "Error closing stream", e);
                }
            }
        }
        return returnString;
    }

    private String _getDetailsJSONFromAPI(Uri uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String returnString = getJSONStringFromAPI(uri, urlConnection);

        return (returnString == null) ? null : returnString;
    }

    @Nullable
    public String getDetailsJSONFromAPI(String id){
        Uri builtUri = Uri.parse(getString(R.string.tmdb_mg_BASE_URL)).buildUpon()
                .appendPath(id)
                .appendQueryParameter("api_key", BuildConfig.API_KEY_THEMOVIEDB)
                .build();

        return _getDetailsJSONFromAPI(builtUri);
    }

    @Nullable
    public String getDetailsJSONFromAPI(String id, String path){
        Uri builtUri = Uri.parse(getString(R.string.tmdb_mg_BASE_URL)).buildUpon()
                .appendPath(id)
                .appendPath(path)
                .appendQueryParameter("api_key", BuildConfig.API_KEY_THEMOVIEDB)
                .build();

        return _getDetailsJSONFromAPI(builtUri);
    }
}
