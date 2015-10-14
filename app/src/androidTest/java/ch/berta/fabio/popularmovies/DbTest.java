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

package ch.berta.fabio.popularmovies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie;
import ch.berta.fabio.popularmovies.data.storage.MovieDbHelper;

/**
 * Created by fabio on 12.10.15.
 */
public class DbTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        final HashSet<String> tablesNames = new HashSet<>();
        tablesNames.add(Movie.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want? 
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: The database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created 
        do {
            tablesNames.remove(c.getString(0));
        } while (c.moveToNext());

        c.close();

        assertTrue("Error: Database was created without the required tables", tablesNames.isEmpty());

        // now, do our tables contain the correct columns? 
        c = db.rawQuery("PRAGMA table_info(" + Movie.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for table information.", c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for 
        final HashSet<String> columns = new HashSet<>();
        columns.add(Movie._ID);
        columns.add(Movie.COLUMN_DB_ID);
        columns.add(Movie.COLUMN_TITLE);
        columns.add(Movie.COLUMN_RELEASE_DATE);
        columns.add(Movie.COLUMN_VOTE_AVERAGE);
        columns.add(Movie.COLUMN_PLOT);
        columns.add(Movie.COLUMN_POSTER);
        columns.add(Movie.COLUMN_BACKDROP);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columns.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all  columns", columns.isEmpty());
        db.close();
    }

    public void testInsertRow() throws Throwable {
        ContentResolver contentResolver = mContext.getContentResolver();

        ContentValues contentValues = getMovieEntry();
        Uri uri = contentResolver.insert(Movie.CONTENT_URI, contentValues);

        Cursor c = contentResolver.query(Movie.buildMovieUri(ContentUris.parseId(uri)), null, null, null, null);
        assertTrue("Insert failed", c.moveToFirst());

        Set<Map.Entry<String, Object>> values = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : values) {
            String columnName = entry.getKey();
            int idx = c.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found.", idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'.", expectedValue, c.getString(idx));
        }
        c.close();
    }

    @NonNull
    private ContentValues getMovieEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Movie.COLUMN_DB_ID, 239744);
        contentValues.put(Movie.COLUMN_TITLE, "Matrix");
        contentValues.put(Movie.COLUMN_RELEASE_DATE, 394203074);
        contentValues.put(Movie.COLUMN_VOTE_AVERAGE, 7.4);
        contentValues.put(Movie.COLUMN_PLOT, "crazy movie");
        contentValues.put(Movie.COLUMN_POSTER, "someUrl");
        contentValues.put(Movie.COLUMN_BACKDROP, "someOtherUrl");
        return contentValues;
    }
}
