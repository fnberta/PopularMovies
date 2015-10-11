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

package ch.berta.fabio.popularmovies.taskfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.MovieDbClient;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Queries TheMoviesDB for movies.
 * <p>
 * A {@link Fragment} with a single task: to perform an online query for movies from the
 * TheMovieDB. It is retained across configuration changes and reports back to its activity
 * via the callback interface {@link ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment.TaskInteractionListener}.
 * </p>
 */
public class QueryMoviesTaskFragment extends Fragment {

    private static final String LOG_TAG = QueryMoviesTaskFragment.class.getSimpleName();
    private static final String BUNDLE_PAGE = "bundle_page";
    private static final String BUNDLE_SORT = "bundle_sort";
    private TaskInteractionListener mListener;
    private Call<MoviesPage> mLoadMoviePosters;

    public QueryMoviesTaskFragment() {
        // required empty constructor
    }

    /**
     * Instantiates and returns a new {@link QueryMoviesTaskFragment} with a page and sort options
     * as parameters.
     *
     * @param page the page number to be used for the movie query
     * @param sort the sort option to be used for the movie query
     * @return a new instance of a {@link QueryMoviesTaskFragment}
     */
    public static QueryMoviesTaskFragment newInstance(int page, String sort) {
        QueryMoviesTaskFragment fragment = new QueryMoviesTaskFragment();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_PAGE, page);
        args.putString(BUNDLE_SORT, sort);
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

        Bundle args = getArguments();
        int page = 0;
        String sort = "";
        if (args != null) {
            page = args.getInt(BUNDLE_PAGE);
            sort = args.getString(BUNDLE_SORT);
        }

        if (page > 0 && !TextUtils.isEmpty(sort)) {
            queryMovies(page, sort);
        } else if (mListener != null) {
            mListener.onMovieQueryFailed();
        }
    }

    private void queryMovies(int page, String sort) {
        mLoadMoviePosters = MovieDbClient.getService().loadMoviePosters(page, sort,
                getString(R.string.movie_db_key));
        mLoadMoviePosters.enqueue(new Callback<MoviesPage>() {
            @Override
            public void onResponse(Response<MoviesPage> response, Retrofit retrofit) {
                MoviesPage moviesPage = response.body();
                if (mListener != null) {
                    if (moviesPage != null) {
                        mListener.onMoviesQueried(moviesPage.getMovies());
                    } else {
                        mListener.onMovieQueryFailed();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mListener != null) {
                    mListener.onMovieQueryFailed();
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
        mLoadMoviePosters.cancel();
        super.onDestroy();
    }

    /**
     * Handles the interaction of the TaskFragment.
     */
    public interface TaskInteractionListener {
        /**
         * Handles the event when movie query finished.
         *
         * @param movies the {@link List<Movie>} containing the queried movies
         */
        void onMoviesQueried(List<Movie> movies);

        /**
         * Handles the event when movie query failed.
         */
        void onMovieQueryFailed();
    }
}
