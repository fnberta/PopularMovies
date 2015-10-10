/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.berta.fabio.popularmovies.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;

public class MovieDetailsFragment extends Fragment {

    private static final String BUNDLE_MOVIE = "bundle_movie";
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private ImageView mImageViewPoster;
    private TextView mTextViewPlot;
    private TextView mTextViewDate;
    private TextView mTextViewRating;
    private Movie mMovie;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_MOVIE, movie);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(BUNDLE_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageViewPoster = (ImageView) view.findViewById(R.id.iv_details_poster);
        loadPoster();

        mTextViewPlot = (TextView) view.findViewById(R.id.tv_details_plot);
        mTextViewPlot.setText(mMovie.getOverview());

        mTextViewDate = (TextView) view.findViewById(R.id.tv_details_release_date);
        loadDate();

        mTextViewRating = (TextView) view.findViewById(R.id.tv_details_rating);
        mTextViewRating.setText(getString(R.string.details_rating, mMovie.getVoteAverage()));
    }

    private void loadPoster() {
        String poster = mMovie.getPosterPath();
        if (!TextUtils.isEmpty(poster)) {
            String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_POSTER_SIZE + poster;
            Glide.with(this)
                    .load(imagePath)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewPoster) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            super.onResourceReady(resource, glideAnimation);
                            ActivityCompat.startPostponedEnterTransition(getActivity());
                        }
                    });
        } else {
            mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER);
            mImageViewPoster.setImageResource(R.drawable.ic_movie_white_72dp);
            ActivityCompat.startPostponedEnterTransition(getActivity());
        }
    }

    private void loadDate() {
        String date = mMovie.getReleaseDateFormatted(true);
        if (!TextUtils.isEmpty(date)) {
            mTextViewDate.setText(date);
        } else {
            mTextViewDate.setVisibility(View.GONE);
        }
    }
}
