/*
 * Copyright (c) 2015 Fabio Berta
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

package ch.berta.fabio.popularmovies.data.repositories

import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.support.v4.content.CursorLoader
import ch.berta.fabio.popularmovies.data.services.MovieService
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.services.dtos.Video
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import rx.Observable
import rx.exceptions.Exceptions
import java.util.*
import javax.inject.Inject

/**
 * Handles the loading of [Movie] data from online sources as well as from the locale content
 * provider. Full separation between loading and display is not possible with [CursorLoader]
 * and adapters using the cursor instead of POJOs. This is a best effort to provide some kind of
 * separation.
 */
class MovieRepository @Inject constructor(
        private val app: Application,
        private val movieService: MovieService
) {
    /**
     * Update a movie from the local content provider with new data fetched online.

     * @param movieDetails the new online data
     * *
     * @param movieRowId   the row id of the movie to update
     */
    fun updateMovieLocal(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): Observable<Array<ContentProviderResult>> = Observable.just(movieDetails)
            .map { getUpdateContentProvidersOps(it, movieRowId) }
            .map {
                try {
                    val contentResolver = app.applicationContext.contentResolver
                    contentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, it)
                } catch (t: Throwable) {
                    throw Exceptions.propagate(t)
                }
            }

    private fun getUpdateContentProvidersOps(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): ArrayList<ContentProviderOperation> = getMovieOps(movieDetails, movieRowId)
            .plus(getMovieReviewsOps(movieDetails, movieRowId))
            .plus(getMovieVideosOps(movieDetails, movieRowId)) as ArrayList

    private fun getMovieOps(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): List<ContentProviderOperation> = listOf(
            ContentProviderOperation
                    .newUpdate(MovieContract.Movie.buildMovieUri(movieRowId))
                    .withValues(movieDetails.getContentValuesEntry())
                    .build()
    )

    private fun getMovieReviewsOps(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): List<ContentProviderOperation> = listOf(
            ContentProviderOperation
                    .newDelete(MovieContract.Review.buildReviewsFromMovieUri(movieRowId))
                    .build()
    )
            .plus(movieDetails.reviewsPage.reviews
                    .map {
                        ContentProviderOperation
                                .newInsert(MovieContract.Review.contentUri)
                                .withValue(MovieContract.Review.COLUMN_MOVIE_ID, movieRowId)
                                .withValues(it.getContentValuesEntry())
                                .build()
                    })

    private fun getMovieVideosOps(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): List<ContentProviderOperation> = listOf(
            ContentProviderOperation
                    .newDelete(MovieContract.Video.buildVideosFromMovieUri(movieRowId))
                    .build()
    )
            .plus(movieDetails.videosPage.videos
                    .filter(Video::siteIsYouTube)
                    .map {
                        ContentProviderOperation
                                .newInsert(MovieContract.Video.contentUri)
                                .withValue(MovieContract.Video.COLUMN_MOVIE_ID, movieRowId)
                                .withValues(it.getContentValuesEntry())
                                .build()
                    })
}
