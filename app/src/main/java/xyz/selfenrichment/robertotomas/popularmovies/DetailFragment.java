package xyz.selfenrichment.robertotomas.popularmovies;
// Created by RobertoTomás on 0004, 4, 4, 2016.

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import xyz.selfenrichment.robertotomas.popularmovies.SQLite.FavoritesContract;
import xyz.selfenrichment.robertotomas.popularmovies.SQLite.FavoritesHelper;
import xyz.selfenrichment.robertotomas.popularmovies.lib.CustomLayout;
import xyz.selfenrichment.robertotomas.popularmovies.lib.LayoutUtil;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;
import xyz.selfenrichment.robertotomas.popularmovies.lib.PicassoUtil;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Review;
import xyz.selfenrichment.robertotomas.popularmovies.lib.ReviewsAdapter;
import xyz.selfenrichment.robertotomas.popularmovies.lib.RoutingDirector;
import xyz.selfenrichment.robertotomas.popularmovies.lib.TrailersAdapter;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Video;
import xyz.selfenrichment.robertotomas.popularmovies.service.AbstractService;
import xyz.selfenrichment.robertotomas.popularmovies.service.GenresService;
import xyz.selfenrichment.robertotomas.popularmovies.service.ReviewsService;
import xyz.selfenrichment.robertotomas.popularmovies.service.TrailersService;


/**
 * The detail fragment shows movie details. It contains a textview mTitle that also houses the
 * details necessary to restore the detail view when transitioning to two pane mode.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        CompoundButton.OnCheckedChangeListener {
    private String TITLE;
    private int POSITION;
    private TextView mTextView;
    private ReviewsAdapter mReviewsAdapter;
    private TrailersAdapter mTrailersAdapter;
    private RoutingDirector mRoutingDirector;
    private Movie mMovie;
    private View mRootView;
    private String mId;

    public static final int FAVORITES_LOADER_ID = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mReviewsAdapter = new ReviewsAdapter(getContext(), new ArrayList<Review>());
        mTrailersAdapter = new TrailersAdapter(getContext(), new ArrayList<Video>());
        mRoutingDirector = new RoutingDirector(getContext());

        if (mRootView == null) {
            mRootView = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_detail,
                    container, false);
        }

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = arguments.getParcelable(getString(R.string.key_grid_view_movie));
            POSITION = arguments.getInt(getString(R.string.key_grid_view_position),0);
        } else {
            Log.e(DetailFragment.class.getSimpleName(), " in onCreateView: Called " +
                    "without arguments");
            return mRootView;
        }

        if(mMovie != null) {
            mId = mMovie.getId();
        }

        float[] screen = LayoutUtil.getScreen(getContext());
        int h = LayoutUtil.convertDPtoPixels(screen[1], getContext());
        int w = LayoutUtil.convertDPtoPixels(screen[0], getContext());
        //Log.d(LOG_TAG, "imageView dimensions: " + w + " x " + h);

        if (mMovie == null) {
            mRootView.setVisibility(View.INVISIBLE);
            return mRootView;
        }else{
            mRootView.setVisibility(View.VISIBLE);
        }

        ImageView iv = (ImageView) mRootView.findViewById(R.id.imagev_details_movie_poster);
        setupPoster(mMovie, h, w, iv);

        setupBackdrop(mMovie, h, w);

        TextView tv = (TextView) mRootView.findViewById(R.id.textv_movie_title);
        setupMovieTitle(mMovie, tv);

        // this has to happen after mId and mTitle are set:
        getActivity().getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);

        tv = (TextView) mRootView.findViewById(R.id.textv_movie_release_date);
        setupReleaseDate(mMovie, tv);

        CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_favorites);
        cb.setOnCheckedChangeListener(this);

        getLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);

        return mRootView;
    }

    private void setupPoster(Movie movie, int h, int w, ImageView iv) {
        if (LayoutUtil.twoPanes(getContext())) {
            iv.setVisibility(View.GONE);
            return;
        } else{
            iv.setVisibility(View.VISIBLE);
        }
        iv.setBackgroundColor(Color.LTGRAY);
        iv.getLayoutParams().height = (h/2) - 1;
        iv.getLayoutParams().width = (w/2) - 1;

        PicassoUtil.attachPoster(getContext(), iv, movie.getId(), movie.getPoster_type(),
                movie.getPoster_path(), movie.getTitle(), h, w);
    }

    private void setupBackdrop(Movie movie, int h, int w) {
        if (!movie.getBackdrop_path().contentEquals("null")) {
            CustomLayout details_layout = (CustomLayout) mRootView.findViewById(
                    R.id.movie_details_custom_layout);
            PicassoUtil.attachPosterToLayout(getContext(), details_layout, movie.getId(),
                    movie.getBackdrop_path(), movie.getTitle(), h, w);
        }
    }

    private void setupMovieTitle(Movie movie, TextView tv) {
        TITLE = movie.getTitle();
        tv.setText(TITLE);
        tv.setTag(R.id.tag_grid_view_item_position, POSITION);
        tv.setTag(R.id.tag_view_item_movie, movie);

        tv = (TextView) mRootView.findViewById(R.id.textv_vote_average);
        tv.setText(movie.getVote_average());
        Float avg_rating = Float.valueOf(movie.getVote_average());
        if (avg_rating > 6.5) {
            tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark)); // color Accent
        } else if (avg_rating > 5) {
            tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_orange_dark));// color Primary
        } else if (avg_rating > 4) {
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));// color PrimaryDark
        }

        tv = (TextView) mRootView.findViewById(R.id.tv_movie_description);
        tv.setText(movie.getOverview());
    }

    private void setupReleaseDate(Movie movie, TextView tv) {
        SimpleDateFormat fdf = new SimpleDateFormat("yyyy-mm-dd");
        SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        try {
            tv.setText(df.format(fdf.parse(movie.getRelease_date())));
        }catch(Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onStart() {
        Log.d(this.getClass().getSimpleName(), "in onStart");

        super.onStart();

        if(mMovie == null) {
            //Log.d(LOG_TAG, "skipping queries for null movie");
            return;
        }
        /** genres */
        mRoutingDirector.initService(
                GenresService.class.getSimpleName(),
                GenresService.class,
                GenresService.INTENTEXTRA_ID,
                mId,
                new RoutingDirector.RoutingCallback() {
                    @Override public void callback(Intent i) { update_genres(i); }
                }
        );

        /** trailers */
        mRoutingDirector.initService(
                TrailersService.class.getSimpleName(),
                TrailersService.class,
                TrailersService.INTENTEXTRA_ID,
                mId,
                new RoutingDirector.RoutingCallback() {
                    @Override public void callback(Intent i) { update_trailers(i); }
                }
        );
        setAdapter(mTrailersAdapter, R.id.listview_moviedetails_trailers);

        /** reviews */
// OLD METHOD FOR REFERENCE:
//        BroadcastReceiver reviewsReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Review[] reviews = (Review[]) intent.getParcelableArrayExtra(AbstractService.HANDLER_RESULT);
//                update_reviews(reviews);
//            }
//        };
//        LocalBroadcastManager.getInstance(this)
//                .registerReceiver(reviewsReceiver, new IntentFilter(ReviewsService.class.getSimpleName()));
//        intent = new Intent(this, ReviewsService.class);
//        intent.putExtra(ReviewsService.INTENTEXTRA_ID, mId);
//        startService(intent);
        mRoutingDirector.initService(
                ReviewsService.class.getSimpleName(),
                ReviewsService.class,
                ReviewsService.INTENTEXTRA_ID,
                mId,
                new RoutingDirector.RoutingCallback() {
                    @Override public void callback(Intent i) { update_reviews(i); }
                }
        );
        setAdapter(mReviewsAdapter, R.id.listview_moviedetails_reviews);

        /** favorites */

        mRoutingDirector.startServices();
    }

    private void setAdapter(ArrayAdapter adapter, int listview_id) {
        ListView listView = (ListView) mRootView.findViewById(listview_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }

        listView.setAdapter(adapter);
    }

    public void update_genres(Intent intent) {
        TextView textView = (TextView) mRootView.findViewById(R.id.textv_movie_genres);
        textView.setText(intent.getStringExtra(AbstractService.HANDLER_RESULT));
    }

    public void update_trailers(Intent intent){
        Video[] videos = (Video[]) intent.getParcelableArrayExtra(AbstractService.HANDLER_RESULT);

        if (videos != null) {
            ListView lv = (ListView) mRootView.findViewById(R.id.listview_moviedetails_trailers);

            mTrailersAdapter.clear();
            for (Video trailer : videos) {
                Log.d(this.getClass().getSimpleName(), "'"+trailer.getSource()+"' is a " +
                        trailer.getType() + " called '" + trailer.getName() + "'");

                mTrailersAdapter.add(trailer);
            }
            LayoutUtil.setListViewHeightBasedOnChildren(lv);
        }
    }

    public void update_reviews(Intent intent){
        Review[] reviews = (Review[]) intent
                .getParcelableArrayExtra(AbstractService.HANDLER_RESULT);

        if (reviews != null) {
            mReviewsAdapter.clear();
            for (Review review : reviews) {
                Log.d(this.getClass().getSimpleName(), "'" + review.getId() +
                        "' is a review by " + review.getAuthor());
                mReviewsAdapter.add(review);
            }
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(this.getClass().getSimpleName(), "in onCreateLoader");

        if (mMovie==null) return null;

        return new CursorLoader(
                getContext(),
                FavoritesContract.FavoritesEntry.ContentUris.CONTENT_URI,
                        new String[] {FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY,
                        FavoritesContract.FavoritesEntry.COL_TITLE},
                        FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + " = ?",
                        new String[] { mId },
                        null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_favorites);

        cb.setOnCheckedChangeListener(null);

        Boolean returnToggle = false;
        try {
            if (!(cursor.moveToFirst()) || (cursor.getCount() == 0)){
                //Log.d( LOG_TAG, "onLoadFinished (Favorites) no results - cnt: " + Integer.toString(cursor.getCount()) );
            } else {
                returnToggle = true;
                //Log.d(LOG_TAG, "#results: "+Integer.toString(cursor.getCount())+" ,movie with tmbd_id: " + cursor.getString(0) + " and title of: " + cursor.getString(1));
            }
        } catch(Exception e) {
            Log.w(this.getClass().getSimpleName(), "onLoadFinished (Favorites) Error: " +
                    e.getMessage() );
        } finally {
            cursor.close();
        }
        cb.setChecked(returnToggle);

        cb.setOnCheckedChangeListener(this);
    }

    @Override
    public void onLoaderReset(Loader loader) { }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Log.v(this.getClass().getSimpleName(), "Adding '" + TITLE + "' to favorites");

            getContext().getContentResolver().insert(
                    FavoritesContract.FavoritesEntry.ContentUris.CONTENT_URI,
                    FavoritesHelper.serializeMovie(mMovie));
        } else {
            Log.v(this.getClass().getSimpleName(), "Removing '" +
                    TITLE + "' (" + mId + ") from favorites");

            getContext().getContentResolver().delete(
                    FavoritesContract.FavoritesEntry.ContentUris.CONTENT_URI,
                    FavoritesContract.FavoritesEntry.COL_THEMOVIEDBKEY + " = ?",
                    new String[]{mId});
        }
    }
}