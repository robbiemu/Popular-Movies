package xyz.selfenrichment.robertotomas.popularmovies.lib;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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
public class ReviewsAdapter extends ArrayAdapter {
    private final String LOG_TAG = ImageWithTitleAdapter.class.getSimpleName();

    private static Boolean reviewsToggleState = false;

    private Context mContext;
    private LayoutInflater inflater;

    private ArrayList<Review> mReviews;

    public ReviewsAdapter(Context context, ArrayList<Review> reviews) {
        super(context, R.layout.linear_trailers, reviews);

        mContext = context;
        mReviews = reviews;

        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Review review = mReviews.get(position);
//        final String id_of_movie = review.getId_of_movie();
        final String id = review.getId();
        final String author = review.getAuthor();
        final String content = review.getContent();
        final String url = review.getUrl();

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.linear_reviews, parent, false);
        }

        convertView.setTag(review);

    // BEGIN inner elements config
        // contentView and convertView need visual distinction
        TextView authorView = (TextView) convertView.findViewById(R.id.textView_review_author);
        authorView.setText(author);

        TextView contentView = (TextView) convertView.findViewById(R.id.textView_review_content);
        contentView.setText(content);
    // END inner elements config

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewsToggleState = !reviewsToggleState;
                /** TODO - make intent expand/revert-from the size of the view to the full height
                 *   required to show the content, and also show/hide the author tag. Finally, if
                 *   there was a URL, show/hide a button to launch an intent to open the URL.
                 */
                LinearLayout ll = (LinearLayout) v;
                ListView lv = (ListView) ll.getParent();

                // toggle author on expand
                for (int i = 0; i < lv.getChildCount(); i++) {
                    LinearLayout ill = (LinearLayout) lv.getChildAt(i);

                    TextView tv = (TextView) ill.findViewById(R.id.textView_review_author);
                    tv.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                    tv.setVisibility(reviewsToggleState?View.GONE:View.VISIBLE);
                }

                // expand element
                if (reviewsToggleState) {
                    ll.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                } else {
                    ll.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }
                LayoutUtil.setListViewHeightBasedOnChildren(lv);
            }
        });

        return convertView;

    }
}
