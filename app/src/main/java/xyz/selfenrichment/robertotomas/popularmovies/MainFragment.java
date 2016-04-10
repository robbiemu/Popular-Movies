package xyz.selfenrichment.robertotomas.popularmovies;
//Created by RobertoTomás on 0004, 4, 4, 2016.

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import xyz.selfenrichment.robertotomas.popularmovies.lib.ImageWithTitleAdapter;
import xyz.selfenrichment.robertotomas.popularmovies.lib.LayoutUtil;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;
import xyz.selfenrichment.robertotomas.popularmovies.service.AbstractService;
import xyz.selfenrichment.robertotomas.popularmovies.service.FavoritesService;
import xyz.selfenrichment.robertotomas.popularmovies.service.MoviesService;

/**
 * Loads the Main activity GridView as a Fragment
 */
public class MainFragment extends Fragment {
    private GridView mGridView;
    private ImageWithTitleAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the following is necessary if we want a menu, since the activity just has this fragment,
        // not even an outer layout
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Integer pos = null;
        if (savedInstanceState == null) {
            Intent intent = getActivity().getIntent();
            if(intent != null) {
                pos = intent.getIntExtra(getString(R.string.key_grid_view_position), 0);
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
        mGridView.setNumColumns(getColumns(mGridView));

        OverScrollDecoratorHelper.setUpOverScroll(mGridView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mGridView.setNestedScrollingEnabled(true);
        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
//                android.R.layout.simple_list_item_1, Data.TITLES);

        mAdapter = new ImageWithTitleAdapter(getContext(), new ArrayList<Movie>());
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((Callback) getActivity())
                        .onItemSelected(v, position);
            }
        });

        if(pos != null) {
            mGridView.scrollTo(mGridView.getScrollX(), pos);
        }

        return rootView;
    }

    private int getColumns(GridView mGridView) {
        int cols = 2;
        if(LayoutUtil.isTablet(getContext())){
            cols = 3;
        }
        if(LayoutUtil.twoPanes(getContext())){
            cols = 1;
        }
        return cols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        fetchPosters();
    }

    private void fetchPosters() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Movie[] movies = (Movie[]) intent.getParcelableArrayExtra(
                        AbstractService.HANDLER_RESULT);
                if (movies.length > 0) {
                    update_posters(movies);
                }
            }
        };
        if(Prefs.getBoolean("pref_posterboard_listing", getResources()
                .getBoolean(R.bool.pref_summary_default_posterboard_listing))) {

            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,
                            new IntentFilter(FavoritesService.class.getSimpleName()));
            getActivity().startService(new Intent(getContext(), FavoritesService.class));
        } else {
            // default is false => to *not* show favorites instead of TMDB
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,
                            new IntentFilter(MoviesService.class.getSimpleName()));
            getActivity().startService(new Intent(getContext(), MoviesService.class));
        }
    }

    public void update_posters(Movie[] movies){
        if (movies != null) {
            mAdapter.clear();
            for (Movie movie : movies) {
                mAdapter.add(movie);
            }
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(View view, int pos);
    }
}
