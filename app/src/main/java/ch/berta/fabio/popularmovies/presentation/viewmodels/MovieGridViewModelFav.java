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

import android.support.annotation.NonNull;
import android.view.View;

import ch.berta.fabio.popularmovies.domain.models.Sort;

/**
 * Defines a view model for a list of locally stored favoured movie posters.
 * <p/>
 * Extends {@link MovieGridViewModel}.
 */
public interface MovieGridViewModelFav extends
        MovieGridViewModel<MovieGridViewModelFav.ViewInteractionListener> {

    /**
     * Returns whether the dbId belongs to the currently selected movie.
     *
     * @param dbId the dbId to check
     * @return whether the dbId belongs to the currently selected movie
     */
    boolean isMovieSelected(int dbId);

    /**
     * Defines the interaction with the view.
     */
    interface ViewInteractionListener extends MovieGridViewModel.ViewInteractionListener {
        /**
         * Launches the detail screen for a movie
         *
         * @param moviePosition       the position of the movie in the view's adapter
         * @param posterSharedElement the poster image to use for the shared element transition
         */
        void launchDetailsScreen(int moviePosition, @NonNull View posterSharedElement);

        /**
         * Switches the main view to show the movies queried online.
         *
         * @param sortSelected the sort option selected
         */
        void showOnlineMovies(@NonNull Sort sortSelected);
    }
}
