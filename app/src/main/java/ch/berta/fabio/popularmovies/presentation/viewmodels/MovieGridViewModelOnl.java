/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.presentation.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.mugen.MugenCallbacks;

import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.SnackbarAction;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMoviesWorker;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMoviesWorkerListener;

/**
 * Defines a view model for a list of online fetched movie posters.
 * <p/>
 * Extends {@link MovieGridViewModel}.
 */
public interface MovieGridViewModelOnl
        extends MovieGridViewModel<MovieGridViewModelOnl.ViewInteractionListener>,
        MugenCallbacks, AdapterViewModel, QueryMoviesWorkerListener {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;
    int MOVIE_DB_MAX_PAGE = 1000;

    @Bindable
    boolean isRefreshing();

    void setRefreshing(boolean refreshing);

    SwipeRefreshLayout.OnRefreshListener getOnRefreshListener();

    void onSortOptionSelected(@NonNull Sort sortSelected);

    void loadMovies();

    /**
     * Returns the movie at the specified position.
     *
     * @param position the position of the movie
     * @return the movie at the specified position
     */
    Movie getMovieAtPosition(int position);

    /**
     * Returns whether the movie is currently selected by the user.
     *
     * @param movie the movie to check
     * @return whether the movie is currently selected by the user
     */
    boolean isMovieSelected(@NonNull Movie movie);

    /**
     * Defines the interaction with the view.
     */
    interface ViewInteractionListener {
        /**
         * Shows a snackbar with an optional click action
         *
         * @param text   the text to display
         * @param action the click action
         */
        void showSnackbar(@StringRes int text, @Nullable SnackbarAction action);

        /**
         * Scrolls the view to the position.
         *
         * @param position the position to scroll to
         */
        void scrollToPosition(int position);

        /**
         * Notifies the view that movies changed.
         */
        void notifyMoviesChanged();

        /**
         * Notifies the view that movies were inserted.
         *
         * @param positionStart position where movies where inserted
         * @param itemCount     number of movies that were inserted
         */
        void notifyMoviesInserted(int positionStart, int itemCount);

        /**
         * Shows the load more progress bar.
         */
        void notifyLoadMoreInserted(int position);

        /**
         * Hides the load more progress bar.
         */
        void notifyLoadMoreRemoved(int position);

        /**
         * Creates a new {@link QueryMoviesWorker} if it is not being retained across a
         * configuration change to query movies from TheMovieDB.
         *
         * @param moviePage     the movie page to query
         * @param sortOption    the sort option to query movies for
         * @param forceNewQuery whether to force a new query when there is already one going on
         */
        void loadQueryMoviesWorker(int moviePage, @NonNull String sortOption, boolean forceNewQuery);

        /**
         * Removes the {@link QueryMoviesWorker} instance from the activity's fragment stack.
         */
        void removeQueryMoviesWorker();

        /**
         * Launches the detail screen for a movie
         *
         * @param movie               the movie to show the details for
         * @param posterSharedElement the poster image to use for the shared element transition
         */
        void launchDetailsScreen(@NonNull Movie movie, @NonNull View posterSharedElement);
    }
}
