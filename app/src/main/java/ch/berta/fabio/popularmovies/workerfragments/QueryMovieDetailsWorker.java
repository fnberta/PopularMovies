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

package ch.berta.fabio.popularmovies.workerfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.MovieDbClient;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Queries TheMoviesDB for movies.
 * <p>
 * A {@link Fragment} with a single task: to perform an online query for movies from the
 * TheMovieDB. It is retained across configuration changes and reports back to its activity
 * via the callback interface {@link QueryMovieDetailsWorker.TaskInteractionListener}.
 * </p>
 */
public class QueryMovieDetailsWorker extends Fragment {

    private static final String LOG_TAG = QueryMovieDetailsWorker.class.getSimpleName();
    private static final String BUNDLE_MOVIE_DB_ID = "BUNDLE_MOVIE_DB_ID";
    private TaskInteractionListener mListener;
    private Call<MovieDetails> mLoadMovieDetails;

    public QueryMovieDetailsWorker() {
        // required empty constructor
    }

    public static QueryMovieDetailsWorker newInstance(int movieDbId) {
        QueryMovieDetailsWorker fragment = new QueryMovieDetailsWorker();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_MOVIE_DB_ID, movieDbId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TaskInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        int movieDbId = -1;
        Bundle args = getArguments();
        if (args != null) {
            movieDbId = args.getInt(BUNDLE_MOVIE_DB_ID, -1);
        }

        if (movieDbId != -1) {
            queryMovieDetails(movieDbId);
        } else if (mListener != null) {
            mListener.onMovieDetailsQueryFailed();
        }
    }

    private void queryMovieDetails(int movieDbId) {
        mLoadMovieDetails = MovieDbClient.getService().loadMovieDetails(movieDbId,
                getString(R.string.movie_db_key), "reviews,videos");
        mLoadMovieDetails.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(Response<MovieDetails> response, Retrofit retrofit) {
                MovieDetails movieDetails = response.body();
                if (mListener != null) {
                    if (movieDetails != null) {
                        mListener.onMovieDetailsQueried(movieDetails);
                    } else {
                        mListener.onMovieDetailsQueryFailed();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mListener != null) {
                    mListener.onMovieDetailsQueryFailed();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        mLoadMovieDetails.cancel();
        super.onDestroy();
    }

    /**
     * Handles the interaction of the TaskFragment with its hosting activity.
     */
    public interface TaskInteractionListener {
        /**
         * Handles a successful movie details query
         *
         * @param movieDetails the queried movie details
         */
        void onMovieDetailsQueried(MovieDetails movieDetails);

        /**
         * Handles a failed movie details query.
         */
        void onMovieDetailsQueryFailed();
    }
}
