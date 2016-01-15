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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.domain.models.Sort;

/**
 * Provides an abstract base class that implements {@link MovieGridViewModel}.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public abstract class MovieGridViewModelBaseImpl<T extends MovieGridViewModel.ViewInteractionListener>
        extends ViewModelBaseImpl<T>
        implements MovieGridViewModel<T> {

    private static final String STATE_MOVIES_AVAILABLE = "STATE_MOVIES_AVAILABLE";
    private static final String STATE_MOVIES_LOADED = "STATE_MOVIES_LOADED";
    private static final String STATE_USER_SELECTED_MOVIE = "STATE_USER_SELECTED_MOVIE";
    private static final String STATE_DB_ID_SELECTED = "STATE_DB_ID_SELECTED";
    int mMovieDbIdSelected;
    private boolean mMoviesAvailable;
    private boolean mMoviesLoaded;
    private boolean mUserSelectedMovie;

    public MovieGridViewModelBaseImpl(@Nullable Bundle savedState) {
        if (savedState != null) {
            mMoviesAvailable = savedState.getBoolean(STATE_MOVIES_AVAILABLE);
            mMoviesLoaded = savedState.getBoolean(STATE_MOVIES_LOADED);
            mUserSelectedMovie = savedState.getBoolean(STATE_USER_SELECTED_MOVIE);
            mMovieDbIdSelected = savedState.getInt(STATE_DB_ID_SELECTED);
        }
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_MOVIES_AVAILABLE, mMoviesAvailable);
        outState.putBoolean(STATE_MOVIES_LOADED, mMoviesLoaded);
        outState.putBoolean(STATE_USER_SELECTED_MOVIE, mUserSelectedMovie);
        outState.putInt(STATE_DB_ID_SELECTED, mMovieDbIdSelected);
    }

    @Override
    @Bindable
    public boolean isMoviesAvailable() {
        return mMoviesAvailable;
    }

    @Override
    public void setMoviesAvailable(boolean moviesAvailable) {
        mMoviesAvailable = moviesAvailable;
        notifyPropertyChanged(BR.moviesAvailable);
    }

    @Override
    @Bindable
    public boolean isMoviesLoaded() {
        return mMoviesLoaded;
    }

    @Override
    public void setMoviesLoaded(boolean moviesLoaded) {
        mMoviesLoaded = moviesLoaded;
        notifyPropertyChanged(BR.moviesLoaded);
    }

    @Override
    @Bindable
    public boolean isUserSelectedMovie() {
        return mUserSelectedMovie;
    }

    @Override
    public void setUserSelectedMovie(boolean userSelectedMovie) {
        mUserSelectedMovie = userSelectedMovie;
        notifyPropertyChanged(BR.userSelectedMovie);
    }

    @Override
    public void onSortSelected(AdapterView<?> parent, View view, int position, long id) {
        final Sort sort = ((Sort) parent.getSelectedItem());
        mView.persistSort(sort, position);
        mView.hideDetailsView();
        switchSort(sort);
    }

    protected abstract void switchSort(@NonNull Sort sort);
}
