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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.storage.ContentProviderHandler;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Displays detail information about a movie, including poster image, release date, rating and
 * an overview of the plot.
 */
public class MovieDetailsFragment extends Fragment  implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ContentProviderHandler.HandlerInteractionListener {

    private static final String KEY_MOVIE = "MOVIE";
    private static final String KEY_ROW_ID = "ROW_ID";
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private static final int FAV_MOVIES_LOADER = 0;
    private static final String[] FAV_MOVIE_COLUMNS = new String[]{
            MovieContract.Movie._ID,
            MovieContract.Movie.COLUMN_DB_ID,
    };
    private static final int COL_INDEX_ROW_ID = 0;
    private static final int COL_INDEX_DB_ID = 1;
    private FragmentInteractionListener mListener;
    private ImageView mImageViewPoster;
    private View mViewHeader;
    private ImageView mImageViewHeaderBackdrop;
    private TextView mTextViewHeaderTitle;
    private TextView mTextViewPlot;
    private TextView mTextViewDate;
    private TextView mTextViewRating;
    private Movie mMovie;
    private long mMovieRowId;
    private AsyncQueryHandler mQueryHandler;
    private boolean mUseTwoPane;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of a {@link MovieDetailsFragment} with a {@link Movie} as an argument.
     *
     * @param movie the {@link Movie} object to be set as an argument
     * @return a new instance of a {@link MovieDetailsFragment}
     */
    public static MovieDetailsFragment newInstance(Movie movie, long movieRowId) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_MOVIE, movie);
        args.putLong(KEY_ROW_ID, movieRowId);
        fragment.setArguments(args);

        return fragment;
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

        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(KEY_MOVIE);
            mMovieRowId = args.getLong(KEY_ROW_ID, -1);
        }

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        mQueryHandler = new ContentProviderHandler(getActivity().getContentResolver(), this);
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

        if (mUseTwoPane) {
            mViewHeader = view.findViewById(R.id.fl_details_header);
            mViewHeader.setVisibility(View.VISIBLE);
            mImageViewHeaderBackdrop = (ImageView) view.findViewById(R.id.iv_details_backdrop);
            mTextViewHeaderTitle = (TextView) view.findViewById(R.id.tv_details_title);
            loadTwoPaneHeader();
            mListener.showFab();
        }
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

    private void loadTwoPaneHeader() {
        String backdrop = mMovie.getBackdropPath();
        if (!TextUtils.isEmpty(backdrop)) {
            String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_BACKDROP_SIZE + backdrop;
            Glide.with(this)
                    .load(imagePath)
                    .into(mImageViewHeaderBackdrop);
        }

        mTextViewHeaderTitle.setText(mMovie.getTitle());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mMovieRowId == -1) {
            getLoaderManager().initLoader(FAV_MOVIES_LOADER, null, this);
        } else {
            setFavoured(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.Movie.buildMovieByDbIdUri(mMovie.getId()),
                FAV_MOVIE_COLUMNS,
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean isFavoured = data.moveToFirst();
        setFavoured(isFavoured);

        if (isFavoured) {
            mMovieRowId = data.getLong(COL_INDEX_ROW_ID);
        }
    }

    private void setFavoured(boolean isFavoured) {
        mMovie.setIsFavoured(isFavoured);
        mListener.toggleFabImage(isFavoured);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    public void toggleFavorite() {
        boolean newlyFavoured;
        if (mMovie.isFavoured()) {
            newlyFavoured = false;
            mQueryHandler.startDelete(
                    0,
                    null,
                    MovieContract.Movie.buildMovieUri(mMovieRowId),
                    null,
                    null);
        } else {
            newlyFavoured = true;
            mQueryHandler.startInsert(
                    0,
                    null,
                    MovieContract.Movie.CONTENT_URI,
                    mMovie.getContentValuesEntry());
        }

        setFavoured(newlyFavoured);
    }

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
        Utils.showBasicSnackbar(mTextViewPlot, getString(R.string.snackbar_added_to_favorites));
    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        Utils.showBasicSnackbar(mTextViewPlot, getString(R.string.snackbar_removed_from_favorites));

        mListener.hideDetailsFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void toggleFabImage(boolean isFavoured);

        void showFab();

        void hideDetailsFragment();
    }
}
