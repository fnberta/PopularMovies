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
import android.view.View;
import android.widget.AdapterView;

import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.ui.adapters.listeners.MovieInteractionListener;

/**
 * Defines a view model for a list of movie posters.
 */
public interface MovieGridViewModel<T extends MovieGridViewModel.ViewInteractionListener> extends ViewModel<T>, MovieInteractionListener {

    @Bindable
    boolean isMoviesAvailable();

    void setMoviesAvailable(boolean moviesAvailable);

    @Bindable
    boolean isMoviesLoaded();

    void setMoviesLoaded(boolean moviesLoaded);

    @Bindable
    boolean isUserSelectedMovie();

    void setUserSelectedMovie(boolean movieSelected);

    void onSortSelected(AdapterView<?> parent, View view, int position, long id);

    interface ViewInteractionListener extends ViewModel.ViewInteractionListener {
        /**
         * Persists the currently selected sort option across app restarts.
         *
         * @param sort     the sort option selected
         * @param position the position of the option
         */
        void persistSort(@NonNull Sort sort, int position);

        /**
         * Hides the view that displays the details of a movie in two-pane (tablets) mode.
         */
        void hideDetailsView();
    }
}
