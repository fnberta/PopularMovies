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

package ch.berta.fabio.popularmovies.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Displays detail information about a movie, including poster image, release date, rating and
 * an overview of the plot.
 */
public class FavMovieDetailsFragment extends BaseMovieDetailsFragment {

    private static final String KEY_MOVIE_ROW_ID = "KEY_MOVIE_ROW_ID";
    private static final String LOG_TAG = FavMovieDetailsFragment.class.getSimpleName();
    private static final int LOADER_FAV = 0;

    public static final String[] FAV_MOVIE_COLUMNS = new String[]{
            MovieContract.Movie.COLUMN_DB_ID,
            MovieContract.Movie.COLUMN_TITLE,
            MovieContract.Movie.COLUMN_RELEASE_DATE,
            MovieContract.Movie.COLUMN_VOTE_AVERAGE,
            MovieContract.Movie.COLUMN_PLOT,
            MovieContract.Movie.COLUMN_POSTER,
            MovieContract.Movie.COLUMN_BACKDROP,
            MovieContract.Review.TABLE_NAME + "." + MovieContract.Review._ID,
            MovieContract.Review.COLUMN_AUTHOR,
            MovieContract.Review.COLUMN_CONTENT,
            MovieContract.Review.COLUMN_URL,
            MovieContract.Video.TABLE_NAME + "." + MovieContract.Video._ID,
            MovieContract.Video.COLUMN_NAME,
            MovieContract.Video.COLUMN_KEY,
            MovieContract.Video.COLUMN_SITE,
            MovieContract.Video.COLUMN_SIZE,
            MovieContract.Video.COLUMN_TYPE
    };
    public static final int COL_INDEX_MOVIE_DB_ID = 0;
    public static final int COL_INDEX_MOVIE_TITLE = 1;
    public static final int COL_INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int COL_INDEX_MOVIE_VOTE_AVERAGE = 3;
    public static final int COL_INDEX_MOVIE_PLOT = 4;
    public static final int COL_INDEX_MOVIE_POSTER = 5;
    public static final int COL_INDEX_MOVIE_BACKDROP = 6;
    public static final int COL_INDEX_REVIEW_ID = 7;
    public static final int COL_INDEX_REVIEW_AUTHOR = 8;
    public static final int COL_INDEX_REVIEW_CONTENT = 9;
    public static final int COL_INDEX_REVIEW_URL = 10;
    public static final int COL_INDEX_VIDEO_ID = 11;
    public static final int COL_INDEX_VIDEO_NAME = 12;
    public static final int COL_INDEX_VIDEO_KEY = 13;
    public static final int COL_INDEX_VIDEO_SITE = 14;
    public static final int COL_INDEX_VIDEO_SIZE = 15;
    public static final int COL_INDEX_VIDEO_TYPE = 16;

    public FavMovieDetailsFragment() {
        // Required empty public constructor
    }

    public static FavMovieDetailsFragment newInstance(long movieRowId) {
        FavMovieDetailsFragment fragment = new FavMovieDetailsFragment();

        Bundle args = new Bundle();
        args.putLong(KEY_MOVIE_ROW_ID, movieRowId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMovieRowId = args.getLong(KEY_MOVIE_ROW_ID, -1);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_FAV, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                MovieContract.Movie.buildMovieWithReviewsAndTrailersUri(mMovieRowId),
                FAV_MOVIE_COLUMNS,
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mMovie = getMovieFromCursor(data);

            setFavoured(true);
            setMovieInfo();
            // TODO: show trailers and reviews
        } else {
            Log.e(LOG_TAG, "cant move to first");
        }
    }

    private Movie getMovieFromCursor(Cursor cursor) {
        final int dbId = cursor.getInt(COL_INDEX_MOVIE_DB_ID);
        final String title = cursor.getString(COL_INDEX_MOVIE_TITLE);
        final Date releaseDate = new Date(cursor.getLong(COL_INDEX_MOVIE_RELEASE_DATE));
        final String poster = cursor.getString(COL_INDEX_MOVIE_POSTER);
        final String backdrop = cursor.getString(COL_INDEX_MOVIE_BACKDROP);
        final String plot = cursor.getString(COL_INDEX_MOVIE_PLOT);
        final double rating = cursor.getDouble(COL_INDEX_MOVIE_VOTE_AVERAGE);

        List<Review> reviews = new ArrayList<>();
        int reviewIdCounter = 0;
        List<Video> videos = new ArrayList<>();
        int videoIdCounter = 0;
        do {
            if (cursor.isNull(COL_INDEX_REVIEW_ID)) {
                final int id = cursor.getInt(COL_INDEX_REVIEW_ID);
                final String author = cursor.getString(COL_INDEX_REVIEW_AUTHOR);
                final String content = cursor.getString(COL_INDEX_REVIEW_CONTENT);
                final String url = cursor.getString(COL_INDEX_REVIEW_URL);

                if (id != reviewIdCounter) {
                    reviewIdCounter= id;
                    reviews.add(new Review(author, content, url));
                }
            } else if (cursor.isNull(COL_INDEX_VIDEO_ID)) {
                final int id = cursor.getInt(COL_INDEX_VIDEO_ID);
                final String name = cursor.getString(COL_INDEX_VIDEO_NAME);
                final String key = cursor.getString(COL_INDEX_VIDEO_KEY);
                final String site = cursor.getString(COL_INDEX_VIDEO_SITE);
                final int size = cursor.getInt(COL_INDEX_VIDEO_SIZE);
                final String type = cursor.getString(COL_INDEX_VIDEO_TYPE);

                if (id != videoIdCounter) {
                    videoIdCounter = id;
                    videos.add(new Video(name, key, site, size, type));
                }
            }
        } while (cursor.moveToNext());

        return new Movie(videos, backdrop, dbId, plot, releaseDate, poster, title, rating, reviews);
    }
}
