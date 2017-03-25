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
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.support.v4.content.CursorLoader
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.MovieService
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.services.dtos.Review
import ch.berta.fabio.popularmovies.data.services.dtos.Video
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.extensions.getDoubleFromColumn
import ch.berta.fabio.popularmovies.extensions.getIntFromColumn
import ch.berta.fabio.popularmovies.extensions.getLongFromColumn
import ch.berta.fabio.popularmovies.extensions.getStringFromColumn
import org.jetbrains.anko.db.asMapSequence
import rx.Observable
import rx.Single
import rx.exceptions.Exceptions
import timber.log.Timber
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
     * Loads a list of movies from TheMovieDB, including their basic information.

     * @param page the page of movies to load
     * *
     * @param sort the sorting scheme to decide which movies to load
     */
    fun getMoviesOnline(page: Int, sort: String): Single<List<Movie>> =
            movieService.loadMoviePosters(page, sort, app.getString(R.string.movie_db_key))
                    .map { (_, movies) -> movies }

    /**
     * Loads the detail information of a movie from TheMovieDB.

     * @param movieDbId the db id of the movie
     */
    fun getMovieDetailsOnline(movieDbId: Int): Single<MovieDetails> =
            movieService.loadMovieDetails(movieDbId, app.getString(R.string.movie_db_key),
                    "reviews,videos")

    /**
     * Returns a [CursorLoader] that loads the details of a user's favourite movie.

     * @param context    the context to use in the loader
     * *
     * @param movieRowId the row id of the movie
     * *
     * @return Returns a [CursorLoader] that loads the details of a user's favourite movie
     */
    fun getFavMovieDetailsLoader(context: Context, movieRowId: Long): CursorLoader {
        val columns = arrayOf(
                MovieContract.Movie.COLUMN_DB_ID,
                MovieContract.Movie.COLUMN_TITLE,
                MovieContract.Movie.COLUMN_RELEASE_DATE,
                MovieContract.Movie.COLUMN_VOTE_AVERAGE,
                MovieContract.Movie.COLUMN_PLOT,
                MovieContract.Movie.COLUMN_POSTER,
                MovieContract.Movie.COLUMN_BACKDROP,
                "${MovieContract.Review.TABLE_NAME}.${BaseColumns._ID}",
                MovieContract.Review.COLUMN_AUTHOR,
                MovieContract.Review.COLUMN_CONTENT,
                MovieContract.Review.COLUMN_URL,
                "${MovieContract.Video.TABLE_NAME}.${BaseColumns._ID}",
                MovieContract.Video.COLUMN_NAME,
                MovieContract.Video.COLUMN_KEY,
                MovieContract.Video.COLUMN_SITE,
                MovieContract.Video.COLUMN_SIZE,
                MovieContract.Video.COLUMN_TYPE
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

    /**
     * Returns a new [Movie] object from the data in the cursor.

     * @param cursor the cursor to get the data from
     * *
     * @return a new [Movie] object
     */
    fun getMovieFromFavMovieDetailsCursor(cursor: Cursor): Movie {
        val dbId = cursor.getIntFromColumn(MovieContract.Movie.COLUMN_DB_ID)
        val title = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_TITLE)
        val releaseDate = Date(cursor.getLongFromColumn(MovieContract.Movie.COLUMN_RELEASE_DATE))
        val poster = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_POSTER)
        val backdrop = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_BACKDROP)
        val plot = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_PLOT)
        val rating = cursor.getDoubleFromColumn(MovieContract.Movie.COLUMN_VOTE_AVERAGE)

        val movie = Movie(backdrop, dbId, plot, releaseDate, poster, title, rating, true,
                emptyList(), emptyList())
        return cursor.asMapSequence()
                .fold(movie, { movie, map ->
                    Timber.d("map: %s", map)
                    when {
                        map["${MovieContract.Review.TABLE_NAME}.${BaseColumns._ID}"] != null ->
                            movie.copy(reviews = movie.reviews.plus(getReview(cursor)))
                        map["${MovieContract.Video.TABLE_NAME}.${BaseColumns._ID}"] != null ->
                            movie.copy(videos = movie.videos.plus(getVideo(cursor)))
                        else -> movie
                    }
                })
    }

    private fun getReview(cursor: Cursor): Review {
        val author = cursor.getStringFromColumn(MovieContract.Review.COLUMN_AUTHOR)
        val content = cursor.getStringFromColumn(
                MovieContract.Review.COLUMN_CONTENT)
        val url = cursor.getStringFromColumn(MovieContract.Review.COLUMN_URL)
        return Review(author, content, url)
    }

    private fun getVideo(cursor: Cursor): Video {
        val name = cursor.getStringFromColumn(MovieContract.Video.COLUMN_NAME)
        val key = cursor.getStringFromColumn(MovieContract.Video.COLUMN_KEY)
        val site = cursor.getStringFromColumn(MovieContract.Video.COLUMN_SITE)
        val size = cursor.getIntFromColumn(MovieContract.Video.COLUMN_SIZE)
        val type = cursor.getStringFromColumn(MovieContract.Video.COLUMN_TYPE)
        return Video(name, key, site, size, type)
    }

    /**
     * Returns a [CursorLoader] that loads only the row id of a movie to check if it exists in
     * the database or not.

     * @param context   the context to use in the loader
     * *
     * @param movieDbId the db id of the movie to load
     * *
     * @return a [CursorLoader] that loads only the row id of a movie
     */
    fun getIsFavLoader(context: Context, movieDbId: Int): CursorLoader = CursorLoader(
            context,
            MovieContract.Movie.buildMovieByDbIdUri(movieDbId),
            arrayOf(BaseColumns._ID),
            null,
            null,
            MovieContract.Movie.SORT_DEFAULT
    )

    /**
     * Returns the row id of the movie via the data of the cursor. Must be a cursor obtained via
     * the [.getIsFavLoader] method.

     * @param cursor the cursor to get the data from
     * *
     * @return the row id of the movie
     */
    fun getRowIdFromIsFavCursor(cursor: Cursor): Long = cursor.getLong(0)

    /**
     * Inserts a [Movie] into the local content provider.

     * @param movie the movie to insert
     */
    fun insertMovieLocal(movie: Movie): Observable<Array<ContentProviderResult>> =
            Observable.just(movie)
                    .map { getInsertContentProviderOps(it) }
                    .map({
                        try {
                            val contentResolver = app.applicationContext.contentResolver
                            contentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, it)
                        } catch (t: Throwable) {
                            throw Exceptions.propagate(t)
                        }
                    })

    private fun getInsertContentProviderOps(movie: Movie): ArrayList<ContentProviderOperation> {
        val insertMovie = listOf(
                ContentProviderOperation
                        .newInsert(MovieContract.Movie.contentUri)
                        .withValues(movie.getContentValuesEntry())
                        .build()
        )

        val reviews =
                movie.reviews.map {
                    ContentProviderOperation
                            .newInsert(MovieContract.Review.contentUri)
                            .withValueBackReference(MovieContract.Review.COLUMN_MOVIE_ID, 0)
                            .withValues(it.getContentValuesEntry())
                            .build()
                }

        val videos = movie.videos
                .filter(Video::siteIsYouTube)
                .map {
                    ContentProviderOperation
                            .newInsert(MovieContract.Video.contentUri)
                            .withValueBackReference(MovieContract.Video.COLUMN_MOVIE_ID, 0)
                            .withValues(it.getContentValuesEntry())
                            .build()
                }

        return insertMovie.plus(reviews).plus(videos) as ArrayList
    }

    /**
     * Deletes a movie from the local content provider.

     * @param movieRowId the row id of the movie to delete
     */
    fun deleteMovieLocal(movieRowId: Long): Observable<Int> = Observable.just(movieRowId)
            .map {
                val contentResolver = app.applicationContext.contentResolver
                contentResolver.delete(MovieContract.Movie.buildMovieUri(it), null, null)
            }

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
            .map({
                try {
                    val contentResolver = app.applicationContext.contentResolver
                    contentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, it)
                } catch (t: Throwable) {
                    throw Exceptions.propagate(t)
                }
            })

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
    ): List<ContentProviderOperation> {
        val delete = listOf(
                ContentProviderOperation
                        .newDelete(MovieContract.Review.buildReviewsFromMovieUri(movieRowId))
                        .build()
        )

        val reviews = movieDetails.reviewsPage.reviews
                .map {
                    ContentProviderOperation
                            .newInsert(MovieContract.Review.contentUri)
                            .withValue(MovieContract.Review.COLUMN_MOVIE_ID, movieRowId)
                            .withValues(it.getContentValuesEntry())
                            .build()
                }

        return delete.plus(reviews)
    }

    private fun getMovieVideosOps(
            movieDetails: MovieDetails,
            movieRowId: Long
    ): List<ContentProviderOperation> {
        val delete = listOf(
                ContentProviderOperation
                        .newDelete(MovieContract.Video.buildVideosFromMovieUri(movieRowId))
                        .build()
        )

        val videos = movieDetails.videosPage.videos
                .filter(Video::siteIsYouTube)
                .map {
                    ContentProviderOperation
                            .newInsert(MovieContract.Video.contentUri)
                            .withValue(MovieContract.Video.COLUMN_MOVIE_ID, movieRowId)
                            .withValues(it.getContentValuesEntry())
                            .build()
                }

        return delete.plus(videos)
    }
}
