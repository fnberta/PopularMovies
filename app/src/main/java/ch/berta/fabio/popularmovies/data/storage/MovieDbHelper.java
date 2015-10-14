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

package ch.berta.fabio.popularmovies.data.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie;

/**
 * Created by fabio on 12.10.15.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createMovieTable = "CREATE TABLE IF NOT EXISTS "
                + Movie.TABLE_NAME + " ( "
                + Movie._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Movie.COLUMN_DB_ID + " INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, "
                + Movie.COLUMN_TITLE + " TEXT NOT NULL, "
                + Movie.COLUMN_RELEASE_DATE + " INTEGER, "
                + Movie.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, "
                + Movie.COLUMN_PLOT + " TEXT, "
                + Movie.COLUMN_POSTER + " TEXT, "
                + Movie.COLUMN_BACKDROP + " TEXT "
                + ");";

        db.execSQL(createMovieTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String dropMovieTable = "DROP TABLE IF EXISTS " + Movie.TABLE_NAME;
        db.execSQL(dropMovieTable);
        onCreate(db);
    }
}
