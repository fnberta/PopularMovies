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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import ch.berta.fabio.popularmovies.WorkerUtils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;

/**
 * Displays detail information about a movie, including poster image, release date, rating and
 * an overview of the plot.
 */
public class MovieDetailsFragment extends BaseMovieDetailsFragment {

    private static final String KEY_MOVIE = "KEY_MOVIE";
    private static final String QUERY_MOVIE_DETAILS_WORKER = "QUERY_MOVIE_DETAILS_WORKER";
    private static final String STATE_MOVIE_DETAILS = "STATE_MOVIE_DETAILS";
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private static final int LOADER_IS_FAV = 0;
    private MovieDetails mMovieTrailersReviews;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of a {@link MovieDetailsFragment} with a {@link Movie} as an argument.
     *
     * @param movie the selected {@link Movie}
     * @return a new instance of a {@link MovieDetailsFragment}
     */
    public static MovieDetailsFragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_MOVIE, movie);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(KEY_MOVIE);
        }

        if (savedInstanceState != null) {
            mMovieTrailersReviews = savedInstanceState.getParcelable(STATE_MOVIE_DETAILS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMovieTrailersReviews != null) {
            outState.putParcelable(STATE_MOVIE_DETAILS, mMovieTrailersReviews);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setMovieInfo();
        getLoaderManager().initLoader(LOADER_IS_FAV, null, this);

        if (mMovieTrailersReviews != null) {
            setReviewsAndTrailers();
        } else {
            fetchTrailersAndReviewsWithWorker();
        }
    }

    private void setReviewsAndTrailers() {
        mMovie.setReviews(mMovieTrailersReviews.getReviewsPage().getReviews());
        mMovie.setVideos(mMovieTrailersReviews.getVideosPage().getVideos());
        // TODO: show review and trailers
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                MovieContract.Movie.buildMovieByDbIdUri(mMovie.getDbId()),
                new String[]{MovieContract.Movie._ID},
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean isFavoured = data.moveToFirst();
        setFavoured(isFavoured);

        if (isFavoured) {
            mMovieRowId = data.getLong(0);
        }
    }

    private void fetchTrailersAndReviewsWithWorker() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, QUERY_MOVIE_DETAILS_WORKER);

        if (worker == null) {
            worker = QueryMovieDetailsWorker.newInstance(mMovie.getDbId());
            fragmentManager.beginTransaction()
                    .add(worker, QUERY_MOVIE_DETAILS_WORKER)
                    .commit();
        }
    }

    public void onMovieDetailsQueried(MovieDetails movieDetails) {
        mMovieTrailersReviews = movieDetails;
        setReviewsAndTrailers();
    }

    public void onMovieDetailsQueryFailed() {
        // TODO: do something
    }
}
