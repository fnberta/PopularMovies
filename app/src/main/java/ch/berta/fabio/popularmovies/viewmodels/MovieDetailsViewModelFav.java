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

package ch.berta.fabio.popularmovies.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import ch.berta.fabio.popularmovies.data.models.MovieDetails;

/**
 * Defines a view model for the details screen of a favoured movie.
 * <p/>
 * Extends {@link MovieDetailsViewModel}.
 */
public interface MovieDetailsViewModelFav extends
        MovieDetailsViewModel<MovieDetailsViewModelFav.ViewInteractionListener> {

    @Bindable
    boolean isRefreshing();

    void setRefreshing(boolean refreshing);

    boolean isDataSetAndNotReloading();

    void onMovieDataEmpty();

    SwipeRefreshLayout.OnRefreshListener getOnRefreshListener();

    /**
     * Defines the interaction with the view.
     */
    interface ViewInteractionListener extends MovieDetailsViewModel.ViewInteractionListener {
        /**
         * Updates a movie in the local content provider with the details.
         *
         * @param movieDetails the details to update the movie with
         */
        void updateMovieLocal(@NonNull MovieDetails movieDetails);

        /**
         * Hides the movie details screen.
         */
        void hideDetailsScreen();

        /**
         * Finishes the current screen.
         */
        void finishScreen();
    }
}
