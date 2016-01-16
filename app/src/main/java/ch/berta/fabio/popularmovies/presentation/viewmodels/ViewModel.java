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

import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.berta.fabio.popularmovies.domain.models.SnackbarAction;
import ch.berta.fabio.popularmovies.presentation.workerfragments.BaseWorkerListener;

/**
 * Defines a basic view model that view models for specific screens can inherit from.
 * <p/>
 * Extends {@link Observable}.
 */
public interface ViewModel<T> extends Observable, BaseWorkerListener {

    /**
     * Passes a {@link Bundle} where the view model can save its state in.
     *
     * @param outState the bundle to save the state in
     */
    void saveState(@NonNull Bundle outState);

    /**
     * Attaches the view to the view model.
     *
     * @param view the view to attach
     */
    void attachView(T view);

    /**
     * Detaches the view from the view model.
     */
    void detachView();

    interface ViewInteractionListener {

        /**
         * Shows a message to the user with an optional click action
         *
         * @param text   the text to display
         * @param action the click action
         */
        void showMessage(@StringRes int text, @Nullable SnackbarAction action);

        /**
         * Removes the headless worker fragment.
         *
         * @param workerTag the fragment's tag
         */
        void removeWorker(@NonNull String workerTag);
    }
}
