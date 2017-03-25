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

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import ch.berta.fabio.popularmovies.data.storage.MovieContract.CONTENT_AUTHORITY
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie
import ch.berta.fabio.popularmovies.data.storage.MovieContract.PATH_MOVIE
import ch.berta.fabio.popularmovies.data.storage.MovieContract.PATH_REVIEW
import ch.berta.fabio.popularmovies.data.storage.MovieContract.PATH_VIDEO
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Review
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Video

/**
 * Provides a local content provider, backed by sqlite database.
 */
class MovieProvider : ContentProvider() {

    companion object {
        private const val URI_TYPE_MOVIES = 100
        private const val URI_TYPE_MOVIE_ID = 101
        private const val URI_TYPE_MOVIE_DB_ID = 102
        private const val URI_TYPE_MOVIE_ID_WITH_REVIEWS_TRAILERS = 103
        private const val URI_TYPE_REVIEWS = 200
        private const val URI_TYPE_REVIEW_ID = 201
        private const val URI_TYPE_REVIEWS_FROM_MOVIE_ID = 202
        private const val URI_TYPE_VIDEOS = 300
        private const val URI_TYPE_VIDEO_ID = 301
        private const val URI_TYPE_VIDEOS_FROM_MOVIE_ID = 302
        private val uriMatcher = buildUriMatcher()

        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)

            matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIE, URI_TYPE_MOVIES)
            matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIE + "/#", URI_TYPE_MOVIE_ID)
            matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIE + "/" + Movie.COLUMN_DB_ID + "/#",
                    URI_TYPE_MOVIE_DB_ID)
            matcher.addURI(CONTENT_AUTHORITY, "$PATH_MOVIE/$PATH_REVIEW/$PATH_VIDEO/#",
                    URI_TYPE_MOVIE_ID_WITH_REVIEWS_TRAILERS)

            matcher.addURI(CONTENT_AUTHORITY, PATH_REVIEW, URI_TYPE_REVIEWS)
            matcher.addURI(CONTENT_AUTHORITY, PATH_REVIEW + "/#", URI_TYPE_REVIEW_ID)
            matcher.addURI(CONTENT_AUTHORITY, "$PATH_REVIEW/$PATH_MOVIE/#",
                    URI_TYPE_REVIEWS_FROM_MOVIE_ID)

            matcher.addURI(CONTENT_AUTHORITY, PATH_VIDEO, URI_TYPE_VIDEOS)
            matcher.addURI(CONTENT_AUTHORITY, PATH_VIDEO + "/#", URI_TYPE_VIDEO_ID)
            matcher.addURI(CONTENT_AUTHORITY, "$PATH_VIDEO/$PATH_MOVIE/#",
                    URI_TYPE_VIDEOS_FROM_MOVIE_ID)

            return matcher
        }
    }

    private lateinit var openHelper: SQLiteOpenHelper

    override fun onCreate(): Boolean {
        openHelper = MovieDbHelper(context!!)
        return true
    }

    override fun getType(uri: Uri): String? {
        val match = uriMatcher.match(uri)
        return when (match) {
            URI_TYPE_MOVIES -> Movie.CONTENT_TYPE
            URI_TYPE_MOVIE_ID -> Movie.CONTENT_ITEM_TYPE
            URI_TYPE_MOVIE_DB_ID -> Movie.CONTENT_ITEM_TYPE
            URI_TYPE_MOVIE_ID_WITH_REVIEWS_TRAILERS -> Movie.CONTENT_TYPE
            URI_TYPE_REVIEWS -> Review.CONTENT_TYPE
            URI_TYPE_REVIEW_ID -> Review.CONTENT_ITEM_TYPE
            URI_TYPE_REVIEWS_FROM_MOVIE_ID -> Review.CONTENT_TYPE
            URI_TYPE_VIDEOS -> Video.CONTENT_TYPE
            URI_TYPE_VIDEO_ID -> Video.CONTENT_ITEM_TYPE
            URI_TYPE_VIDEOS_FROM_MOVIE_ID -> Video.CONTENT_TYPE
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
    }

    override fun query(uri: Uri,
                       projection: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       sortOrder: String?
    ): Cursor? {
        val db = openHelper.readableDatabase
        val queryBuilder = SQLiteQueryBuilder()
        val match = uriMatcher.match(uri)
        when (match) {
            URI_TYPE_MOVIES -> queryBuilder.tables = Movie.TABLE_NAME
            URI_TYPE_MOVIE_ID -> {
                queryBuilder.tables = Movie.TABLE_NAME
                queryBuilder.appendWhere("${BaseColumns._ID} = ${ContentUris.parseId(uri)}")
            }
            URI_TYPE_MOVIE_DB_ID -> {
                queryBuilder.tables = Movie.TABLE_NAME
                queryBuilder.appendWhere("${Movie.COLUMN_DB_ID} = ${ContentUris.parseId(uri)}")
            }
            URI_TYPE_MOVIE_ID_WITH_REVIEWS_TRAILERS -> {
                queryBuilder.tables = "${Movie.TABLE_NAME} " +
                        "LEFT JOIN ${Review.TABLE_NAME} " +
                        "ON ${Movie.TABLE_NAME}.${BaseColumns._ID} = ${Review.TABLE_NAME}.${Review.COLUMN_MOVIE_ID} " +
                        "LEFT JOIN ${Video.TABLE_NAME} " +
                        "ON ${Movie.TABLE_NAME}.${BaseColumns._ID} = ${Video.TABLE_NAME}.${Video.COLUMN_MOVIE_ID}"
                queryBuilder.appendWhere(
                        Movie.TABLE_NAME + "." + BaseColumns._ID + " = " + Movie.getMovieIdFromUri(
                                uri))
            }
            URI_TYPE_REVIEWS -> queryBuilder.tables = Review.TABLE_NAME
            URI_TYPE_REVIEW_ID -> {
                queryBuilder.tables = Review.TABLE_NAME
                queryBuilder.appendWhere("${BaseColumns._ID} = ${ContentUris.parseId(uri)}")
            }
            URI_TYPE_REVIEWS_FROM_MOVIE_ID -> {
                queryBuilder.tables = Review.TABLE_NAME
                queryBuilder.appendWhere(
                        "${Review.COLUMN_MOVIE_ID} = ${Review.getMovieIdFromUri(uri)}")
            }
            URI_TYPE_VIDEOS -> queryBuilder.tables = Video.TABLE_NAME
            URI_TYPE_VIDEO_ID -> {
                queryBuilder.tables = Video.TABLE_NAME
                queryBuilder.appendWhere("${BaseColumns._ID} = ${ContentUris.parseId(uri)}")
            }
            URI_TYPE_VIDEOS_FROM_MOVIE_ID -> {
                queryBuilder.tables = Video.TABLE_NAME
                queryBuilder.appendWhere(
                        "${Video.COLUMN_MOVIE_ID} = ${Video.getMovieIdFromUri(uri)}")
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null,
                sortOrder)
        if (context != null) {
            cursor.setNotificationUri(context.contentResolver, uri)
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        val returnUri = when (match) {
            URI_TYPE_MOVIES -> {
                val id = db.insert(Movie.TABLE_NAME, null, values)
                if (id > 0) {
                    Movie.buildMovieUri(id)
                } else throw SQLException("Failed to insert row into $uri")
            }
            URI_TYPE_REVIEWS -> {
                val id = db.insert(Review.TABLE_NAME, null, values)
                if (id > 0) {
                    Review.buildReviewUri(id)
                } else throw SQLException("Failed to insert row into $uri")
            }
            URI_TYPE_VIDEOS -> {
                val id = db.insert(Video.TABLE_NAME, null, values)
                if (id > 0) {
                    Video.buildVideoUri(id)
                } else throw SQLException("Failed to insert row into $uri")
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        notifyChange(uri)
        return returnUri
    }

    private fun notifyChange(uri: Uri) {
        context?.contentResolver?.notifyChange(uri, null)
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val db = openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        return when (match) {
            URI_TYPE_MOVIES -> bulkInsert(uri, values, db, Movie.TABLE_NAME)
            URI_TYPE_REVIEWS -> bulkInsert(uri, values, db, Review.TABLE_NAME)
            URI_TYPE_VIDEOS -> bulkInsert(uri, values, db, Video.TABLE_NAME)
            else -> super.bulkInsert(uri, values)
        }
    }

    private fun bulkInsert(uri: Uri,
                           values: Array<ContentValues>,
                           db: SQLiteDatabase,
                           tableName: String
    ): Int {
        db.beginTransaction()
        val returnCount = try {
            val count = values
                    .map { db.insert(tableName, null, it).toInt() }
                    .filter { it > 0 }
                    .sum()
            db.setTransactionSuccessful()
            count
        } finally {
            db.endTransaction()
        }

        notifyChange(uri)
        return returnCount
    }

    override fun update(uri: Uri,
                        values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<String>?
    ): Int {
        val db = openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        val rowsUpdated = when (match) {
            URI_TYPE_MOVIES -> db.update(Movie.TABLE_NAME, values, selection, selectionArgs)
            URI_TYPE_MOVIE_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.update(Movie.TABLE_NAME, values, where, selectionArgs)
            }
            URI_TYPE_MOVIE_DB_ID -> {
                val where = buildWhereClause(Movie.COLUMN_DB_ID, uri, selection)
                db.update(Movie.TABLE_NAME, values, where, selectionArgs)
            }
            URI_TYPE_REVIEWS -> db.update(Review.TABLE_NAME, values, selection, selectionArgs)
            URI_TYPE_REVIEW_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.update(Review.TABLE_NAME, values, where, selectionArgs)
            }
            URI_TYPE_VIDEOS -> db.update(Video.TABLE_NAME, values, selection, selectionArgs)
            URI_TYPE_VIDEO_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.update(Video.TABLE_NAME, values, where, selectionArgs)
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        if (rowsUpdated > 0) {
            notifyChange(uri)
        }
        return rowsUpdated
    }

    private fun buildWhereClause(columnId: String, uri: Uri, selection: String?): String {
        return "$columnId = ${ContentUris.parseId(uri)}" +
                if (!TextUtils.isEmpty(selection)) " AND $selection" else ""
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        val rowsDeleted = when (match) {
            URI_TYPE_MOVIES -> db.delete(Movie.TABLE_NAME, selection, selectionArgs)
            URI_TYPE_MOVIE_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.delete(Movie.TABLE_NAME, where, selectionArgs)
            }
            URI_TYPE_MOVIE_DB_ID -> {
                val where = buildWhereClause(Movie.COLUMN_DB_ID, uri, selection)
                db.delete(Movie.TABLE_NAME, where, selectionArgs)
            }
            URI_TYPE_REVIEWS -> db.delete(Review.TABLE_NAME, selection, selectionArgs)
            URI_TYPE_REVIEW_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.delete(Review.TABLE_NAME, where, selectionArgs)
            }
            URI_TYPE_REVIEWS_FROM_MOVIE_ID -> {
                val where = buildWhereClause(Review.COLUMN_MOVIE_ID, uri, selection)
                db.delete(Review.TABLE_NAME, where, selectionArgs)
            }
            URI_TYPE_VIDEOS -> db.delete(Video.TABLE_NAME, selection, selectionArgs)
            URI_TYPE_VIDEO_ID -> {
                val where = buildWhereClause(BaseColumns._ID, uri, selection)
                db.delete(Video.TABLE_NAME, where, selectionArgs)
            }
            URI_TYPE_VIDEOS_FROM_MOVIE_ID -> {
                val where = buildWhereClause(Video.COLUMN_MOVIE_ID, uri, selection)
                db.delete(Video.TABLE_NAME, where, selectionArgs)
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            notifyChange(uri)
        }
        return rowsDeleted
    }
}
