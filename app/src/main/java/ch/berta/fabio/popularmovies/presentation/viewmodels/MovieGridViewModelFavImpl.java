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

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Provides an implementation of the {@link MovieGridViewModelFav} interface.
 * <p/>
 * Subclass of {@link MovieGridViewModelBaseImpl}.
 */
public class MovieGridViewModelFavImpl extends
        MovieGridViewModelBaseImpl<MovieGridViewModelFav.ViewInteractionListener> implements
        MovieGridViewModelFav {

    public static final Creator<MovieGridViewModelFavImpl> CREATOR = new Creator<MovieGridViewModelFavImpl>() {
        public MovieGridViewModelFavImpl createFromParcel(Parcel source) {
            return new MovieGridViewModelFavImpl(source);
        }

        public MovieGridViewModelFavImpl[] newArray(int size) {
            return new MovieGridViewModelFavImpl[size];
        }
    };
    private int mMovieDbIdSelected;

    public MovieGridViewModelFavImpl() {
    }

    protected MovieGridViewModelFavImpl(Parcel in) {
        super(in);

        mMovieDbIdSelected = in.readInt();
    }

    @Override
    public boolean isMovieSelected(int dbId) {
        if (dbId == mMovieDbIdSelected) {
            return true;
        }

        mMovieDbIdSelected = dbId;
        return false;
    }

    @Override
    public void onMovieRowItemClick(int position, @NonNull View sharedView) {
        mView.launchDetailsScreen(position, sharedView);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(mMovieDbIdSelected);
    }
}
