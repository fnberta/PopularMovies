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

package ch.berta.fabio.popularmovies.features.details.di

import android.content.Context
import android.provider.BaseColumns
import android.support.v4.content.CursorLoader
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import dagger.Module
import dagger.Provides

@Module
class DetailsFavLoaderModule(val context: Context, val movieRowId: Long) {

    @Provides
    fun providesDetailsFavLoader(): CursorLoader {
        val columns = arrayOf(
                "${MovieContract.Movie.TABLE_NAME}.${BaseColumns._ID}",
                MovieContract.Movie.COLUMN_TITLE,
                MovieContract.Movie.COLUMN_RELEASE_DATE,
                MovieContract.Movie.COLUMN_VOTE_AVERAGE,
                MovieContract.Movie.COLUMN_PLOT,
                MovieContract.Movie.COLUMN_POSTER,
                MovieContract.Movie.COLUMN_BACKDROP,
                MovieContract.Review.COLUMN_AUTHOR,
                MovieContract.Review.COLUMN_CONTENT,
                MovieContract.Video.COLUMN_NAME,
                MovieContract.Video.COLUMN_KEY,
                MovieContract.Video.COLUMN_SITE,
                MovieContract.Video.COLUMN_SIZE
        )

        return CursorLoader(
                context,
                MovieContract.Movie.buildMovieWithReviewsAndTrailersUri(movieRowId),
                columns,
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT
        )
    }
}
