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

import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelFav;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelFavImpl;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelOnlImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the instantiation of the view models for the Movie Details screen.
 */
@Module
public class MovieDetailsViewModelModule {

    Bundle mSavedState;
    Movie mMovie;
    long mRowId;
    boolean mUseTwoPane;

    public MovieDetailsViewModelModule(@Nullable Bundle savedState, @NonNull Movie movie,
                                       boolean useTwoPane) {
        mSavedState = savedState;
        mMovie = movie;
        mUseTwoPane = useTwoPane;
    }

    public MovieDetailsViewModelModule(@Nullable Bundle savedState, long rowId, boolean useTwoPane) {
        mSavedState = savedState;
        mRowId = rowId;
        mUseTwoPane = useTwoPane;
    }

    @Provides
    MovieDetailsViewModelOnl providesMovieDetailsViewModelOnl(@NonNull MovieRepository movieRepository) {
        return new MovieDetailsViewModelOnlImpl(mSavedState, movieRepository, mMovie, mUseTwoPane);
    }

    @Provides
    MovieDetailsViewModelFav providesMovieDetailsViewModelFav(@NonNull MovieRepository movieRepository) {
        return new MovieDetailsViewModelFavImpl(mSavedState, movieRepository, mRowId, mUseTwoPane);
    }
}
