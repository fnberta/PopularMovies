/*
 * Copyright (c) 2015 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies.ui;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Displays detail information about a movie, including poster image, release date, rating and
 * an overview of the plot.
 */
public abstract class BaseMovieDetailsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = BaseMovieDetailsFragment.class.getSimpleName();

    private static final String CRUD_HELPER = "CRUD_HELPER";
    private static final int TOKEN_DELETE = 0;
    FragmentInteractionListener mListener;
    long mMovieRowId;
    boolean mUseTwoPane;
    Movie mMovie;
    private ImageView mImageViewPoster;
    private View mViewHeader;
    private ImageView mImageViewHeaderBackdrop;
    private TextView mTextViewHeaderTitle;
    private TextView mTextViewPlot;
    private TextView mTextViewDate;
    private TextView mTextViewRating;
    private InsertMovieTask mInsertMovieTask;
    private QueryHandler mDeleteMovieHandler;

    public BaseMovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
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
        mTextViewPlot = (TextView) view.findViewById(R.id.tv_details_plot);
        mTextViewDate = (TextView) view.findViewById(R.id.tv_details_release_date);
        mTextViewRating = (TextView) view.findViewById(R.id.tv_details_rating);

        if (mUseTwoPane) {
            mViewHeader = view.findViewById(R.id.fl_details_header);
            mViewHeader.setVisibility(View.VISIBLE);
            mImageViewHeaderBackdrop = (ImageView) view.findViewById(R.id.iv_details_backdrop);
            mTextViewHeaderTitle = (TextView) view.findViewById(R.id.tv_details_title);
            mListener.showFab();
        }
    }

    final void setMovieInfo() {
        setPoster(mMovie.getPosterPath());
        setDate(mMovie.getReleaseDate());
        setPlot(mMovie.getOverview());
        setRating(mMovie.getVoteAverage());
        final String title = mMovie.getTitle();
        final String backdropPath = mMovie.getBackdropPath();
        if (mUseTwoPane) {
            setTwoPaneHeader(title, backdropPath);
        } else {
            mListener.setOnePaneHeader(title, backdropPath);
        }
    }

    private void setPoster(String poster) {
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

    private void setDate(Date date) {
        String dateFormatted = Utils.formatDateLong(date);
        if (!TextUtils.isEmpty(dateFormatted)) {
            mTextViewDate.setText(dateFormatted);
        } else {
            mTextViewDate.setVisibility(View.GONE);
        }
    }

    private void setPlot(String plot) {
        mTextViewPlot.setText(plot);
    }

    private void setRating(double rating) {
        mTextViewRating.setText(getString(R.string.details_rating, rating));
    }

    private void setTwoPaneHeader(String title, String backdrop) {
        mTextViewHeaderTitle.setText(title);

        if (!TextUtils.isEmpty(backdrop)) {
            String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_BACKDROP_SIZE + backdrop;
            Glide.with(this)
                    .load(imagePath)
                    .into(mImageViewHeaderBackdrop);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    final void setFavoured(boolean isFavoured) {
        mMovie.setIsFavoured(isFavoured);
        mListener.toggleFabImage(isFavoured);
    }

    public void toggleFavorite() {
        boolean newlyFavoured;
        if (!mMovie.isFavoured()) {
            newlyFavoured = true;
            mInsertMovieTask = new InsertMovieTask();
            mInsertMovieTask.execute(mMovie);
        } else {
            newlyFavoured = false;
            mDeleteMovieHandler = new QueryHandler(getActivity().getContentResolver());
            mDeleteMovieHandler.startDelete(
                    TOKEN_DELETE,
                    null,
                    MovieContract.Movie.buildMovieUri(mMovieRowId),
                    null,
                    null
            );
        }

        setFavoured(newlyFavoured);
    }

    private void onMovieInserted() {
        Snackbar.make(mTextViewPlot, getString(R.string.snackbar_added_to_favorites), Snackbar.LENGTH_LONG).show();
    }

    private void onMovieDeleted() {
        Snackbar.make(mTextViewPlot, getString(R.string.snackbar_removed_from_favorites),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mInsertMovieTask != null) {
            mInsertMovieTask.cancel(true);
        }

        if (mDeleteMovieHandler != null) {
            mDeleteMovieHandler.cancelOperation(TOKEN_DELETE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    public interface FragmentInteractionListener {
        void toggleFabImage(boolean isFavoured);

        void showFab();

        void setOnePaneHeader(String title, String backdrop);
    }

    private class InsertMovieTask extends AsyncTask<Movie, Integer, ContentProviderResult[]> {

        @Override
        protected ContentProviderResult[] doInBackground(Movie... params) {
            final Movie movie = params[0];

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation
                            .newInsert(MovieContract.Movie.CONTENT_URI)
                            .withValues(movie.getContentValuesEntry())
                            .build()
            );

            List<Review> reviews = movie.getReviews();
            if (reviews != null && !reviews.isEmpty()) {
                for (Review review : reviews) {
                    ops.add(ContentProviderOperation
                                    .newInsert(MovieContract.Review.CONTENT_URI)
                                    .withValueBackReference(MovieContract.Review.COLUMN_MOVIE_ID, 0)
                                    .withValues(review.getContentValuesEntry())
                                    .build()
                    );
                }
            }

            List<Video> videos = movie.getVideos();
            if (videos != null && !videos.isEmpty()) {
                for (Video video : videos) {
                    ops.add(ContentProviderOperation
                                    .newInsert(MovieContract.Video.CONTENT_URI)
                                    .withValueBackReference(MovieContract.Video.COLUMN_MOVIE_ID, 0)
                                    .withValues(video.getContentValuesEntry())
                                    .build()
                    );
                }
            }

            try {
                return getActivity().getContentResolver().applyBatch(MovieContract.CONTENT_AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ContentProviderResult[] contentProviderResults) {
            super.onPostExecute(contentProviderResults);

            if (contentProviderResults != null) {
                onMovieInserted();
            }
        }
    }

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);

            onMovieDeleted();
        }
    }
}
