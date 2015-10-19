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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static ch.berta.fabio.popularmovies.data.storage.MovieContract.CONTENT_AUTHORITY;
import static ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie;
import static ch.berta.fabio.popularmovies.data.storage.MovieContract.PATH_MOVIES;

public class MovieProvider extends ContentProvider {

    private static final int URI_TYPE_MOVIE = 100;
    private static final int URI_TYPE_MOVIE_ID = 101;
    private static final int URI_TYPE_MOVIE_DB_ID = 200;
    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIES, URI_TYPE_MOVIE);
        matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIES + "/#", URI_TYPE_MOVIE_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_MOVIES + "/" + Movie.COLUMN_DB_ID + "/#",
                URI_TYPE_MOVIE_DB_ID);

        return matcher;
    }

    private SQLiteOpenHelper mOpenHelper;

    public MovieProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                return Movie.CONTENT_TYPE;
            case URI_TYPE_MOVIE_ID:
                return Movie.CONTENT_ITEM_TYPE;
            case URI_TYPE_MOVIE_DB_ID:
                return Movie.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                queryBuilder.setTables(Movie.TABLE_NAME);
                // no selection
                break;
            case URI_TYPE_MOVIE_ID:
                queryBuilder.setTables(Movie.TABLE_NAME);
                queryBuilder.appendWhere(Movie._ID + " = " + ContentUris.parseId(uri));
                break;
            case URI_TYPE_MOVIE_DB_ID:
                queryBuilder.setTables(Movie.TABLE_NAME);
                queryBuilder.appendWhere(Movie.COLUMN_DB_ID + " = " + ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                long id = db.insert(Movie.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = Movie.buildMovieUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        notifyChange(uri);
        return returnUri;
    }

    private void notifyChange(@NonNull Uri uri) {
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(Movie.TABLE_NAME, null, value);
                        if (id > 0) {
                            returnCount++;
                        } else {
                            throw new android.database.SQLException("Failed to insert row into " + uri);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                notifyChange(uri);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                rowsUpdated = db.update(Movie.TABLE_NAME, values, selection, selectionArgs);
                break;
            case URI_TYPE_MOVIE_ID: {
                String where = Movie._ID + " = " + ContentUris.parseId(uri);
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsUpdated = db.update(Movie.TABLE_NAME, values, where, selectionArgs);
                break;
            }
            case URI_TYPE_MOVIE_DB_ID: {
                String where = Movie.COLUMN_DB_ID + " = " + ContentUris.parseId(uri);
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsUpdated = db.update(Movie.TABLE_NAME, values, where, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated > 0) {
            notifyChange(uri);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_MOVIE:
                rowsDeleted = db.delete(Movie.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_TYPE_MOVIE_ID: {
                String where = Movie._ID + " = " + ContentUris.parseId(uri);
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsDeleted = db.delete(Movie.TABLE_NAME, where, selectionArgs);
                break;
            }
            case URI_TYPE_MOVIE_DB_ID: {
                String where = Movie.COLUMN_DB_ID + " = " + ContentUris.parseId(uri);
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsDeleted = db.delete(Movie.TABLE_NAME, where, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }
}