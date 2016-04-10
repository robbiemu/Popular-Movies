package xyz.selfenrichment.robertotomas.popularmovies;
// Created by RobertoTomás on 0004, 4, 4, 2016.

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import xyz.selfenrichment.robertotomas.popularmovies.lib.LayoutUtil;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;

/**
 * This activity is meant for single-pane mode only. It has a handler to detect screenRotation. Each
 * rotation event will check if the layout qualifies for two-pane mode and if so, defer back to the
 * activity.
 */
public class DetailActivity extends AppCompatActivity {
    private int mGridViewPosition;
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            Movie movie = i.getParcelableExtra(getString(R.string.key_grid_view_movie));
            int pos = i.getIntExtra(getString(R.string.key_grid_view_position),0);

            Bundle arguments = new Bundle();
            arguments.putParcelable(getString(R.string.key_grid_view_movie), movie);
            arguments.putInt(getString(R.string.key_grid_view_position), pos);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentcontainer_detail, fragment)
                    .commit();
        } else {
            mGridViewPosition = savedInstanceState.getInt(
                    getString(R.string.key_grid_view_position));
            mMovie = savedInstanceState.getParcelable(
                    getString(R.string.key_grid_view_movie));

            if(LayoutUtil.twoPanes(this)){ // if the screen rotated while on single pane, we need to
                // translate to dual-pane presentation.
                startActivity(new Intent(this, MainActivity.class)
                                .putExtra(
                                        getString(R.string.key_grid_view_position),
                                        mGridViewPosition)
                                .putExtra(getString(R.string.key_grid_view_movie), mMovie)
                );
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView tv = (TextView) findViewById(R.id.textv_movie_title);
        outState.putInt(getString(R.string.key_grid_view_position), _getPositionFromTag(tv));
        outState.putParcelable(getString(R.string.key_grid_view_movie), _getMovie(tv));
    }

    // Intellij balks about this possibly throwing a null object ref if it is in onSaveInstanceState
    @NonNull
    private Movie _getMovie(TextView tv) {
        return (Movie) tv.getTag(R.id.tag_view_item_movie);
    }

    @NonNull
    private Integer _getPositionFromTag(TextView tv) {
        return (Integer) tv.getTag(R.id.tag_grid_view_item_position);
    }
}