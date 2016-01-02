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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.List;

import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;

/**
 * Queries TheMoviesDB for movies.
 * <p>
 * A {@link Fragment} with a single task: to perform an online query for movies from the
 * TheMovieDB. It is retained across configuration changes and reports back to its activity
 * via the callback interface {@link WorkerInteractionListener}.
 * </p>
 */
public class QueryMoviesWorker extends Fragment implements
        MovieRepository.GetMoviesOnlineListener {

    private static final String LOG_TAG = QueryMoviesWorker.class.getSimpleName();
    private static final String BUNDLE_PAGE = "bundle_page";
    private static final String BUNDLE_SORT = "bundle_sort";
    @Nullable
    private WorkerInteractionListener mActivity;
    private MovieRepository mMovieRepo;

    public QueryMoviesWorker() {
        // required empty constructor
    }

    /**
     * Returns a new instance of a {@link QueryMoviesWorker} with a page and sort options
     * as arguments.
     *
     * @param page the page number to be used for the movie query
     * @param sort the sort option to be used for the movie query
     * @return a new instance of a {@link QueryMoviesWorker}
     */
    public static QueryMoviesWorker newInstance(int page, @NonNull String sort) {
        QueryMoviesWorker fragment = new QueryMoviesWorker();

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
            mActivity = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WorkerInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mMovieRepo = new MovieRepository();

        Bundle args = getArguments();
        int page = 0;
        String sort = "";
        if (args != null) {
            page = args.getInt(BUNDLE_PAGE, 0);
            sort = args.getString(BUNDLE_SORT, "");
        }

        if (page > 0 && !TextUtils.isEmpty(sort)) {
            mMovieRepo.getMoviesOnline(getActivity(), page, sort, this);
        } else if (mActivity != null) {
            mActivity.onMoviesOnlineLoadFailed();
        }
    }

    @Override
    public void onMoviesOnlineLoaded(@NonNull List<Movie> movies) {
        if (mActivity != null) {
            mActivity.onMoviesOnlineLoaded(movies);
        }
    }

    @Override
    public void onMoviesOnlineLoadFailed() {
        if (mActivity != null) {
            mActivity.onMoviesOnlineLoadFailed();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void onDestroy() {
        mMovieRepo.cancelOnlineLoad();

        super.onDestroy();
    }

    /**
     * Handles the interaction of the WorkerFragment.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the event when movie query finished.
         *
         * @param movies the {@link List<Movie>} containing the queried movies
         */
        void onMoviesOnlineLoaded(@NonNull List<Movie> movies);

        /**
         * Handles the event when movie query failed.
         */
        void onMoviesOnlineLoadFailed();
    }
}
