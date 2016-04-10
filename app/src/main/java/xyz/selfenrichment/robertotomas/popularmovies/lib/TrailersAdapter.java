package xyz.selfenrichment.robertotomas.popularmovies.lib;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

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
public class TrailersAdapter extends ArrayAdapter {
    private final String LOG_TAG = ImageWithTitleAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater inflater;

    private ArrayList<Video> mTrailers;

    public TrailersAdapter(Context context, ArrayList<Video> trailers) {
        super(context, R.layout.linear_trailers, trailers);

        mContext = context;
        mTrailers = trailers;

        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Video video = mTrailers.get(position);
//        final String id_of_movie = video.getId_of_movie();
        final String source = video.getSource();
        final String name = video.getName();
        final String size = video.getSize();
        final String type = video.getType();

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.linear_trailers, parent, false);
        }

        convertView.setTag(video);

        TextView trailerName = (TextView) convertView.findViewById(R.id.textView_trailer_name);
        trailerName.setText(name);

        TextView trailerSize = (TextView) convertView.findViewById(R.id.textView_trailer_size);
        trailerSize.setText(size);

        TextView trailerType = (TextView) convertView.findViewById(R.id.textView_trailer_type);
        trailerType.setText(type);

        ImageButton play = (ImageButton) convertView.findViewById(R.id.imageButton_playMovie);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + source)));
            }
        });

        return convertView;

    }
}
