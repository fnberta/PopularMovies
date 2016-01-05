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
import android.os.Parcel;

import ch.berta.fabio.popularmovies.BR;

/**
 * Provides an abstract base class that implements {@link MovieGridViewModel}.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public abstract class MovieGridViewModelBaseImpl<T>
        extends BaseObservable
        implements MovieGridViewModel<T> {

    T mView;
    private boolean mMoviesAvailable;
    private boolean mMoviesLoaded;
    private boolean mUserSelectedMovie;

    public MovieGridViewModelBaseImpl() {
    }

    protected MovieGridViewModelBaseImpl(Parcel in) {
        mMoviesAvailable = in.readByte() != 0;
        mMoviesLoaded = in.readByte() != 0;
        mUserSelectedMovie = in.readByte() != 0;
    }

    @Override
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mMoviesAvailable ? (byte) 1 : (byte) 0);
        dest.writeByte(mMoviesLoaded ? (byte) 1 : (byte) 0);
        dest.writeByte(mUserSelectedMovie ? (byte) 1 : (byte) 0);
    }
}
