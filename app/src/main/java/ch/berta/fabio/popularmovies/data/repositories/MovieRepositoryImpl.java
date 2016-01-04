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

package ch.berta.fabio.popularmovies.data.repositories;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;
import ch.berta.fabio.popularmovies.data.rest.MovieDbClient;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Provides an implementation of the {@link MovieRepository} interface.
 */
public class MovieRepositoryImpl implements MovieRepository {

    private static final String LOG_TAG = MovieRepositoryImpl.class.getSimpleName();
    private static final int COL_INDEX_MOVIE_ROW_ID = 0;
    private static final int COL_INDEX_MOVIE_DB_ID = 1;
    private static final int COL_INDEX_MOVIE_TITLE = 2;
    private static final int COL_INDEX_MOVIE_RELEASE_DATE = 3;
    private static final int COL_INDEX_MOVIE_POSTER = 4;
    private static final String[] FAV_MOVIES_COLUMNS = new String[]{
            MovieContract.Movie._ID,
            MovieContract.Movie.COLUMN_DB_ID,
            MovieContract.Movie.COLUMN_TITLE,
            MovieContract.Movie.COLUMN_RELEASE_DATE,
            MovieContract.Movie.COLUMN_POSTER,
    };

    private static final int COL_INDEX_MOVIE_DETAILS_DB_ID = 0;
    private static final int COL_INDEX_MOVIE_DETAILS_TITLE = 1;
    private static final int COL_INDEX_MOVIE_DETAILS_RELEASE_DATE = 2;
    private static final int COL_INDEX_MOVIE_DETAILS_VOTE_AVERAGE = 3;
    private static final int COL_INDEX_MOVIE_DETAILS_PLOT = 4;
    private static final int COL_INDEX_MOVIE_DETAILS_POSTER = 5;
    private static final int COL_INDEX_MOVIE_DETAILS_BACKDROP = 6;
    private static final int COL_INDEX_MOVIE_DETAILS_REVIEW_ID = 7;
    private static final int COL_INDEX_MOVIE_DETAILS_REVIEW_AUTHOR = 8;
    private static final int COL_INDEX_MOVIE_DETAILS_REVIEW_CONTENT = 9;
    private static final int COL_INDEX_MOVIE_DETAILS_REVIEW_URL = 10;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_ID = 11;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_NAME = 12;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_KEY = 13;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_SITE = 14;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_SIZE = 15;
    private static final int COL_INDEX_MOVIE_DETAILS_VIDEO_TYPE = 16;
    private static final String[] FAV_MOVIE_DETAILS_COLUMNS = new String[]{
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

    /**
     * Constructs a new {@link MovieRepositoryImpl}.
     */
    public MovieRepositoryImpl() {
    }

    @Override
    public Observable<List<Movie>> getMoviesOnline(@NonNull Context context, int page, @NonNull String sort) {
        return MovieDbClient.getService().loadMoviePosters(page, sort,
                context.getString(R.string.movie_db_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<MoviesPage, List<Movie>>() {
                    @Override
                    public List<Movie> call(MoviesPage moviesPage) {
                        return moviesPage.getMovies();
                    }
                });
    }

    @Override
    public Observable<MovieDetails> getMovieDetailsOnline(@NonNull Context context, int movieDbId) {
        return MovieDbClient.getService().loadMovieDetails(movieDbId,
                context.getString(R.string.movie_db_key), "reviews,videos")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public CursorLoader getFavMoviesLoader(@NonNull Context context) {
        return new CursorLoader(
                context,
                MovieContract.Movie.CONTENT_URI,
                FAV_MOVIES_COLUMNS,
                null,
                null,
                MovieContract.Movie.SORT_BY_RELEASE_DATE
        );
    }

    @Override
    public int getMovieDbIdFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getInt(MovieRepositoryImpl.COL_INDEX_MOVIE_DB_ID);
    }

    @Override
    public String getMovieTitleFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getString(MovieRepositoryImpl.COL_INDEX_MOVIE_TITLE);
    }

    @Override
    public Date getMovieReleaseDateFromFavMoviesCursor(@NonNull Cursor cursor) {
        return new Date(cursor.getLong(MovieRepositoryImpl.COL_INDEX_MOVIE_RELEASE_DATE));
    }

    @Override
    public String getMoviePosterFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getString(MovieRepositoryImpl.COL_INDEX_MOVIE_POSTER);
    }

    @Override
    public CursorLoader getFavMovieDetailsLoader(@NonNull Context context, long movieRowId) {
        return new CursorLoader(
                context,
                MovieContract.Movie.buildMovieWithReviewsAndTrailersUri(movieRowId),
                FAV_MOVIE_DETAILS_COLUMNS,
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT
        );
    }

    @Override
    public Movie getMovieFromFavMovieDetailsCursor(@NonNull Cursor cursor) {
        final int dbId = cursor.getInt(COL_INDEX_MOVIE_DETAILS_DB_ID);
        final String title = cursor.getString(COL_INDEX_MOVIE_DETAILS_TITLE);
        final Date releaseDate = new Date(cursor.getLong(COL_INDEX_MOVIE_DETAILS_RELEASE_DATE));
        final String poster = cursor.getString(COL_INDEX_MOVIE_DETAILS_POSTER);
        final String backdrop = cursor.getString(COL_INDEX_MOVIE_DETAILS_BACKDROP);
        final String plot = cursor.getString(COL_INDEX_MOVIE_DETAILS_PLOT);
        final double rating = cursor.getDouble(COL_INDEX_MOVIE_DETAILS_VOTE_AVERAGE);

        List<Review> reviews = new ArrayList<>();
        int reviewIdCounter = 0;
        List<Video> videos = new ArrayList<>();
        int videoIdCounter = 0;
        do {
            if (!cursor.isNull(COL_INDEX_MOVIE_DETAILS_REVIEW_ID)) {
                final int id = cursor.getInt(COL_INDEX_MOVIE_DETAILS_REVIEW_ID);
                final String author = cursor.getString(COL_INDEX_MOVIE_DETAILS_REVIEW_AUTHOR);
                final String content = cursor.getString(COL_INDEX_MOVIE_DETAILS_REVIEW_CONTENT);
                final String url = cursor.getString(COL_INDEX_MOVIE_DETAILS_REVIEW_URL);

                if (id > reviewIdCounter) {
                    reviewIdCounter = id;
                    reviews.add(new Review(author, content, url));
                }
            }

            if (!cursor.isNull(COL_INDEX_MOVIE_DETAILS_VIDEO_ID)) {
                final int id = cursor.getInt(COL_INDEX_MOVIE_DETAILS_VIDEO_ID);
                final String name = cursor.getString(COL_INDEX_MOVIE_DETAILS_VIDEO_NAME);
                final String key = cursor.getString(COL_INDEX_MOVIE_DETAILS_VIDEO_KEY);
                final String site = cursor.getString(COL_INDEX_MOVIE_DETAILS_VIDEO_SITE);
                final int size = cursor.getInt(COL_INDEX_MOVIE_DETAILS_VIDEO_SIZE);
                final String type = cursor.getString(COL_INDEX_MOVIE_DETAILS_VIDEO_TYPE);

                if (id > videoIdCounter) {
                    videoIdCounter = id;
                    videos.add(new Video(name, key, site, size, type));
                }
            }
        } while (cursor.moveToNext());

        return new Movie(videos, backdrop, dbId, plot, releaseDate, poster, title, rating, reviews, true);
    }

    @Override
    public CursorLoader getIsFavLoader(@NonNull Context context, int movieDbId) {
        return new CursorLoader(
                context,
                MovieContract.Movie.buildMovieByDbIdUri(movieDbId),
                new String[]{MovieContract.Movie._ID},
                null,
                null,
                MovieContract.Movie.SORT_DEFAULT
        );
    }

    @Override
    public long getRowIdFromIsFavCursor(@NonNull Cursor cursor) {
        return cursor.getLong(0);
    }

    @Override
    public Observable<ContentProviderResult[]> insertMovieLocal(@NonNull final Context context,
                                                                @NonNull Movie movie) {
        return Observable.just(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Movie, ArrayList<ContentProviderOperation>>() {
                    @Override
                    public ArrayList<ContentProviderOperation> call(Movie movie) {
                        return getInsertContentProviderOps(movie);
                    }
                })
                .map(new Func1<ArrayList<ContentProviderOperation>, ContentProviderResult[]>() {
                    @Override
                    public ContentProviderResult[] call(ArrayList<ContentProviderOperation> ops) {
                        try {
                            final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
                            return contentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, ops);
                        } catch (Throwable t) {
                            throw Exceptions.propagate(t);
                        }
                    }
                });
    }

    private ArrayList<ContentProviderOperation> getInsertContentProviderOps(@NonNull Movie movie) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newInsert(MovieContract.Movie.CONTENT_URI)
                .withValues(movie.getContentValuesEntry())
                .build()
        );

        List<Review> reviews = movie.getReviews();
        if (!reviews.isEmpty()) {
            for (Review review : reviews) {
                ops.add(ContentProviderOperation
                        .newInsert(MovieContract.Review.CONTENT_URI)
                        .withValueBackReference(MovieContract.Review.COLUMN_MOVIE_ID, 0)
                        .withValues(review.getContentValuesEntry())
                        .build()
                );
            }
        }

        List<Video> videos = movie.getVideos();
        if (!videos.isEmpty()) {
            for (Video video : videos) {
                // only add youtube videos
                if (video.siteIsYouTube()) {
                    ops.add(ContentProviderOperation
                            .newInsert(MovieContract.Video.CONTENT_URI)
                            .withValueBackReference(MovieContract.Video.COLUMN_MOVIE_ID, 0)
                            .withValues(video.getContentValuesEntry())
                            .build()
                    );
                }
            }
        }

        return ops;
    }

    @Override
    public Observable<Integer> deleteMovieLocal(@NonNull final Context context, long movieRowId) {
        return Observable.just(movieRowId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long aLong) {
                        final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
                        return contentResolver.delete(MovieContract.Movie.buildMovieUri(aLong), null, null);
                    }
                });
    }

    @Override
    public Observable<ContentProviderResult[]> updateMovieLocal(@NonNull final Context context,
                                                                @NonNull final MovieDetails movieDetails,
                                                                final long movieRowId) {
        return Observable.just(movieDetails)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<MovieDetails, ArrayList<ContentProviderOperation>>() {
                    @Override
                    public ArrayList<ContentProviderOperation> call(MovieDetails movieDetails) {
                        return getUpdateContentProvidersOps(movieDetails, movieRowId);
                    }
                })
                .map(new Func1<ArrayList<ContentProviderOperation>, ContentProviderResult[]>() {
                    @Override
                    public ContentProviderResult[] call(ArrayList<ContentProviderOperation> contentProviderOperations) {
                        try {
                            final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
                            return contentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY,
                                    contentProviderOperations);
                        } catch (Throwable t) {
                            throw Exceptions.propagate(t);
                        }
                    }
                });
    }

    private ArrayList<ContentProviderOperation> getUpdateContentProvidersOps(
            @NonNull MovieDetails movieDetails, long movieRowId) {
        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        addMovieOps(movieDetails, movieRowId, ops);
        addMovieReviewsOps(movieDetails, movieRowId, ops);
        addMovieVideosOps(movieDetails, movieRowId, ops);

        return ops;
    }

    private void addMovieOps(@NonNull MovieDetails movieDetails,
                             long movieRowId,
                             @NonNull ArrayList<ContentProviderOperation> ops) {
        ops.add(ContentProviderOperation
                .newUpdate(MovieContract.Movie.buildMovieUri(movieRowId))
                .withValues(movieDetails.getContentValuesEntry())
                .build()
        );
    }

    private void addMovieReviewsOps(@NonNull MovieDetails movieDetails,
                                    long movieRowId,
                                    @NonNull ArrayList<ContentProviderOperation> ops) {
        ops.add(ContentProviderOperation
                .newDelete(MovieContract.Review.buildReviewsFromMovieUri(movieRowId))
                .build()
        );

        List<Review> reviews = movieDetails.getReviewsPage().getReviews();
        if (!reviews.isEmpty()) {
            for (Review review : reviews) {
                ops.add(ContentProviderOperation
                        .newInsert(MovieContract.Review.CONTENT_URI)
                        .withValue(MovieContract.Review.COLUMN_MOVIE_ID, movieRowId)
                        .withValues(review.getContentValuesEntry())
                        .build()
                );
            }
        }
    }

    private void addMovieVideosOps(@NonNull MovieDetails movieDetails,
                                   long movieRowId,
                                   @NonNull ArrayList<ContentProviderOperation> ops) {
        ops.add(ContentProviderOperation
                .newDelete(MovieContract.Video.buildVideosFromMovieUri(movieRowId))
                .build()
        );

        List<Video> videos = movieDetails.getVideosPage().getVideos();
        if (!videos.isEmpty()) {
            for (Video video : videos) {
                // only add youtube videos
                if (video.siteIsYouTube()) {
                    ops.add(ContentProviderOperation
                            .newInsert(MovieContract.Video.CONTENT_URI)
                            .withValue(MovieContract.Video.COLUMN_MOVIE_ID, movieRowId)
                            .withValues(video.getContentValuesEntry())
                            .build()
                    );
                }
            }
        }
    }
}
