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

package ch.berta.fabio.popularmovies.features.grid.di

import android.content.Context
import android.provider.BaseColumns
import android.support.v4.content.CursorLoader
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository
import ch.berta.fabio.popularmovies.data.services.MovieService
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.features.grid.loaders.GridOnlMoviesLoader
import dagger.Module
import dagger.Provides

/**
 * Defines the instantiation of the view models for the Movie Grid screen.
 */
@Module
class GridLoaderModule(private val context: Context) {

    @Provides
    fun providesGridOnlMoviesLoader(movieService: MovieService): GridOnlMoviesLoader =
            GridOnlMoviesLoader(context, movieService)

    @Provides
    fun providesGridFavMoviesLoader(): CursorLoader {
        val columns = arrayOf(
                BaseColumns._ID,
                MovieContract.Movie.COLUMN_DB_ID,
                MovieContract.Movie.COLUMN_TITLE,
                MovieContract.Movie.COLUMN_RELEASE_DATE,
                MovieContract.Movie.COLUMN_POSTER
        )

        return CursorLoader(
                context,
                MovieContract.Movie.contentUri,
                columns,
                null,
                null,
                MovieContract.Movie.SORT_BY_RELEASE_DATE
        )
    }
}
