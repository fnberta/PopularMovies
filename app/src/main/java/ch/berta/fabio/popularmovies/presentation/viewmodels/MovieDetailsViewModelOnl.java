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

import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMovieDetailsWorkerListener;

/**
 * Defines a view model for the details screen of an online fetched movie.
 * <p/>
 * Extends {@link MovieDetailsViewModel}.
 */
public interface MovieDetailsViewModelOnl extends
        MovieDetailsViewModel<MovieDetailsViewModelOnl.ViewInteractionListener>,
        QueryMovieDetailsWorkerListener {

    /**
     * Loads the detail information of amovie.
     */
    void loadMovieDetails();

    /**
     * Called when the menu is about to be inflated in the view.
     */
    void onMenuInflation();

    /**
     * Returns the TheMovieDB id of the movie.
     *
     * @return the TheMovieDB id of the movie
     */
    int getMovieDbId();

    interface ViewInteractionListener extends MovieDetailsViewModel.ViewInteractionListener {
        /**
         * Loads the worker fragment that queries for the details of a movie.
         *
         * @param movieDbId the TheMovieDB id of the movie
         */
        void loadQueryMovieDetailsWorker(int movieDbId);

        /**
         * Removes the worker fragment that queried for movie details.
         */
        void removeQueryMovieDetailsWorker();
    }
}
