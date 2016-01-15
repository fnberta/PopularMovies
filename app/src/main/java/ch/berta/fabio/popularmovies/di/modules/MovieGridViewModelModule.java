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

package ch.berta.fabio.popularmovies.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelFav;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelFavImpl;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelOnlImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the instantiation of the view models for the Movie Grid screen.
 */
@Module
public class MovieGridViewModelModule {

    Bundle mSavedState;
    Sort mSortSelected;

    public MovieGridViewModelModule(@Nullable Bundle savedState) {
        mSavedState = savedState;
    }

    public MovieGridViewModelModule(@Nullable Bundle savedState, @NonNull Sort sortSelected) {
        mSavedState = savedState;
        mSortSelected = sortSelected;
    }

    @Provides
    MovieGridViewModelOnl providesMovieGridViewModelOnl() {
        return new MovieGridViewModelOnlImpl(mSavedState, mSortSelected);
    }

    @Provides
    MovieGridViewModelFav providesMovieGridViewModelFav() {
        return new MovieGridViewModelFavImpl(mSavedState);
    }
}