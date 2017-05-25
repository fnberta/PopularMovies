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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Review
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Video

const val DATABASE_NAME = "movies.db"
const val DATABASE_VERSION = 4

/**
 * Provides the instructions to create, upgrade and delete local sqlite database.
 */
class MovieDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null,
        DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createMovieTable = "CREATE TABLE IF NOT EXISTS ${Movie.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Movie.COLUMN_DB_ID} INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                "${Movie.COLUMN_TITLE} TEXT NOT NULL, " +
                "${Movie.COLUMN_RELEASE_DATE} INTEGER, " +
                "${Movie.COLUMN_VOTE_AVERAGE} REAL NOT NULL, " +
                "${Movie.COLUMN_PLOT} TEXT, " +
                "${Movie.COLUMN_POSTER} TEXT, " +
                "${Movie.COLUMN_BACKDROP} TEXT);"

        val createReviewTable = "CREATE TABLE IF NOT EXISTS ${Review.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Review.COLUMN_MOVIE_ID} INTEGER NOT NULL REFERENCES ${Movie.TABLE_NAME} (${BaseColumns._ID}) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "${Review.COLUMN_AUTHOR} TEXT NOT NULL, " +
                "${Review.COLUMN_CONTENT} TEXT NOT NULL, " +
                "${Review.COLUMN_URL} TEXT NOT NULL);"

        val createVideoTable = "CREATE TABLE IF NOT EXISTS ${Video.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Video.COLUMN_MOVIE_ID} INTEGER NOT NULL REFERENCES ${Movie.TABLE_NAME} (${BaseColumns._ID}) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "${Video.COLUMN_NAME} TEXT NOT NULL, " +
                "${Video.COLUMN_KEY} TEXT NOT NULL, " +
                "${Video.COLUMN_SITE} TEXT NOT NULL, " +
                "${Video.COLUMN_SIZE} INTEGER NOT NULL, " +
                "${Video.COLUMN_TYPE} TEXT NOT NULL);"

        val createReviewIdx = "CREATE INDEX ${Review.INDEX_MOVIE_ID} " +
                "ON ${Review.TABLE_NAME}(${Review.COLUMN_MOVIE_ID});"
        val createVideoIdx = "CREATE INDEX ${Video.INDEX_MOVIE_ID} " +
                "ON ${Video.TABLE_NAME}(${Video.COLUMN_MOVIE_ID});"

        db.execSQL(createMovieTable)
        db.execSQL(createReviewTable)
        db.execSQL(createVideoTable)

        db.execSQL(createReviewIdx)
        db.execSQL(createVideoIdx)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropMovieTable = "DROP TABLE IF EXISTS ${Movie.TABLE_NAME}"
        val dropReviewTable = "DROP TABLE IF EXISTS ${Review.TABLE_NAME}"
        val dropVideoTable = "DROP TABLE IF EXISTS ${Video.TABLE_NAME}"
        db.execSQL(dropMovieTable)
        db.execSQL(dropReviewTable)
        db.execSQL(dropVideoTable)

        onCreate(db)
    }
}
