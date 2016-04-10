package xyz.selfenrichment.robertotomas.popularmovies.lib;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import xyz.selfenrichment.robertotomas.popularmovies.R;

/**
 * Created by RobertoTomás on 0019, 19/2/2016.
 *
 * Custom adapter that adds 2 things:
 *  - ArrayAdapter over the Movie type.
 *  - Picasso with scaled images in ArrayAdapter (to populate ImageViews)
 *
 */
public class ImageWithTitleAdapter extends ArrayAdapter {
    private final String LOG_TAG = ImageWithTitleAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater inflater;

    private ArrayList<Movie> mMovies;

    public ImageWithTitleAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.grid_poster, movies);

        mContext = context;
        mMovies = movies;

        inflater = LayoutInflater.from(mContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        final Movie movie = mMovies.get(pos);
        final String id = movie.getId();
        final String type_of_Uri = movie.getPoster_type();
        final String url = movie.getPoster_path();
        final String title = movie.getTitle();

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.grid_poster, parent, false);
        }

        ImageView iv = (ImageView) convertView;

        int h, w;
        {
            float[] screen = LayoutUtil.getScreen(mContext);
            if(LayoutUtil.twoPanes(getContext())) {
                h = LayoutUtil.convertDPtoPixels(screen[1], mContext);
                w = parent.getWidth();
            } else {
                h = LayoutUtil.convertDPtoPixels(screen[1], mContext);
                w = LayoutUtil.convertDPtoPixels(screen[0], mContext);
            }
        }
//        Log.d(LOG_TAG, "imageView dimensions: " + w + " x " + h);

        int cols = ((GridView) parent).getNumColumns();

        iv.setBackgroundColor(Color.LTGRAY);
        iv.getLayoutParams().height = (h/cols) - 1;
        iv.getLayoutParams().width = (w/cols) - 1;

        PicassoUtil.attachPoster(mContext, (ImageView) convertView, id, type_of_Uri, url, title, h, w);
        convertView.setTag(movie);
        convertView.setContentDescription(movie.getTitle());

//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(LOG_TAG,"grid item clicked! movie is: " + ((Movie) v.getTag()).getId());
//                ((DetailsFragment.Callback) getContext())
//                        .onItemSelected((Movie) v.getTag());
//            }
//        });

        return convertView;
    }
}
