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

package ch.berta.fabio.popularmovies.data.storage

import android.content.ContentResolver.CURSOR_DIR_BASE_TYPE
import android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns
import ch.berta.fabio.popularmovies.BuildConfig

/**
 * Defines the structure of the local sqlite database.
 */
object MovieContract {

    const val CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID
    const val PATH_MOVIE = "movie"
    const val PATH_REVIEW = "review"
    const val PATH_VIDEO = "video"

    val baseContentUri: Uri = Uri.parse("content://" + CONTENT_AUTHORITY)

    object Movie : BaseColumns {
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_MOVIE"
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_MOVIE"
        const val TABLE_NAME = "movie"

        const val COLUMN_DB_ID = "db_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_RELEASE_DATE = "release_date"
        const val COLUMN_VOTE_AVERAGE = "vote_average"
        const val COLUMN_PLOT = "plot"
        const val COLUMN_POSTER = "poster"
        const val COLUMN_BACKDROP = "backdrop"

        const val SORT_DEFAULT = "$TABLE_NAME.${BaseColumns._ID} DESC"
        const val SORT_BY_RELEASE_DATE = "$TABLE_NAME.$COLUMN_RELEASE_DATE DESC"

        val contentUri: Uri = baseContentUri.buildUpon().appendPath(PATH_MOVIE).build()

        fun buildMovieUri(id: Long): Uri {
            return ContentUris.withAppendedId(contentUri, id)
        }

        fun buildMovieByDbIdUri(dbId: Int): Uri {
            return contentUri.buildUpon()
                    .appendPath(COLUMN_DB_ID)
                    .appendPath(dbId.toString())
                    .build()
        }

        fun buildMovieWithReviewsAndTrailersUri(id: Long): Uri {
            return contentUri.buildUpon()
                    .appendPath(PATH_REVIEW)
                    .appendPath(PATH_VIDEO)
                    .appendPath(id.toString())
                    .build()
        }

        fun getMovieIdFromUri(uri: Uri): String {
            return uri.pathSegments[3]
        }
    }

    object Review : BaseColumns {
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_REVIEW"
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_REVIEW"
        const val TABLE_NAME = "review"

        const val COLUMN_MOVIE_ID = "movie_id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_URL = "url"
        const val INDEX_MOVIE_ID = TABLE_NAME + "_" + COLUMN_MOVIE_ID + "_idx"

        val contentUri: Uri = baseContentUri.buildUpon().appendPath(PATH_REVIEW).build()

        fun buildReviewUri(id: Long): Uri {
            return ContentUris.withAppendedId(contentUri, id)
        }

        fun buildReviewsFromMovieUri(movieId: Long): Uri {
            return contentUri.buildUpon().appendPath(PATH_MOVIE).appendPath(
                    movieId.toString()).build()
        }

        fun getMovieIdFromUri(uri: Uri): String {
            return uri.pathSegments[2]
        }
    }

    object Video : BaseColumns {
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_VIDEO"
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/$CONTENT_AUTHORITY/$PATH_VIDEO"
        const val TABLE_NAME = "video"

        const val COLUMN_MOVIE_ID = "movie_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_KEY = "key"
        const val COLUMN_SITE = "site"
        const val COLUMN_SIZE = "size"
        const val COLUMN_TYPE = "type"
        const val INDEX_MOVIE_ID = TABLE_NAME + "_" + COLUMN_MOVIE_ID + "_idx"

        val contentUri: Uri = baseContentUri.buildUpon().appendPath(PATH_VIDEO).build()

        fun buildVideoUri(id: Long): Uri {
            return ContentUris.withAppendedId(contentUri, id)
        }

        fun buildVideosFromMovieUri(movieId: Long): Uri {
            return contentUri.buildUpon().appendPath(PATH_MOVIE).appendPath(
                    movieId.toString()).build()
        }

        fun getMovieIdFromUri(uri: Uri): String {
            return uri.pathSegments[2]
        }
    }
}
