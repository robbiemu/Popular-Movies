package xyz.selfenrichment.robertotomas.popularmovies;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pixplicity.easyprefs.library.Prefs;

import xyz.selfenrichment.robertotomas.popularmovies.lib.LayoutUtil;
import xyz.selfenrichment.robertotomas.popularmovies.lib.Movie;

/**
 * Popular movies app, stage 2
 *
 * see: https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.7sxo8jefdfll
 */
public class MainActivity extends AppCompatActivity implements MainFragment.Callback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (LayoutUtil.twoPanes(this)) {
            Intent intent = getIntent();
            if (intent != null) { // if we came here by intent it is from the detail activity
                int i = intent.getIntExtra( getString(R.string.key_grid_view_position), 0);
                Movie m = intent.getParcelableExtra( getString(R.string.key_grid_view_movie) );
                loadDetailInTwoPane(i, m);
            } else if (savedInstanceState == null) {
                // In two-pane mode, show the detail fragment in this activity by adding or replacing
                // the fragmentcontainer in the xml-layout using a fragment transaction.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentcontainer_detail,
                                new Fragment(), DetailFragment.class.getSimpleName())
                        .commit();
            }
        }

        // Initialize the Prefs class
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFab();

        /* the id in the <fragment> is to configure an un-instantiated fragment by id, as from the
           main activity's onCreate with:
        SomeFragment someFragment = ((SomeFragment)getSupportFragmentManager()
                .findFragmentById(R.id.this_fragment_id));
        someFragment.prepareSomeStateForRender(someStateValue);
        */
    }

    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(Prefs.getBoolean("pref_posterboard_listing", getResources()
                .getBoolean(R.bool.pref_summary_default_posterboard_listing))) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Prefs.putBoolean("pref_posterboard_listing", true);
                    Intent intent = getIntent();
                    finish();
                    view.getContext().startActivity(intent);
//                    view.getContext().startActivity(
//                            new Intent(view.getContext(), SettingsActivity.class)
//                                    .putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
//                    );
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFab();
    }

    private void loadDetailInTwoPane(int pos, Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.key_grid_view_movie), movie);
        args.putInt(getString(R.string.key_grid_view_position), pos);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentcontainer_detail, fragment,
                        DetailFragment.class.getSimpleName())
                .commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * MainFragment Callback override - handles click events on gridview items
     * @param v - the calling view containing the data item
     * @param pos - the position of the view from the grid_view
     */
    @Override
    public void onItemSelected(View v, int pos) {
        Movie movie =  (Movie) v.getTag();
        // We are splitting the logic here because we don't just need to load the detail fragment
        // with the data. We also need to completely switch activities if it is single-pane
        if (LayoutUtil.twoPanes(this)) {
            loadDetailInTwoPane(pos, movie);
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(getString(R.string.key_grid_view_movie), movie)
                    .putExtra(getString(R.string.key_grid_view_position), pos);
            startActivity(intent);
        }
    }
}
