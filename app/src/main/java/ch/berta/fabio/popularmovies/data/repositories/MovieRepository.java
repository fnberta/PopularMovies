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

import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.MovieDbClient;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Handles the loading of {@link Movie} data from online sources as well as from the locale content
 * provider. Full separation between loading and display is not possible with {@link CursorLoader}
 * and adapters using the cursor instead of POJOs. This is a best effort to provide some kind of
 * separation.
 */
public class MovieRepository {

    private static final String LOG_TAG = MovieRepository.class.getSimpleName();
    private static final int TOKEN_DELETE = 0;
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

    private Call<MoviesPage> mLoadMovies;
    private Call<MovieDetails> mLoadMovieDetails;
    private InsertMovieTask mInsertMovieTask;
    private UpdateMovieTask mUpdateMovieTask;
    private DeleteMovieHandler mDeleteMovieHandler;

    /**
     * Constructs a new {@link MovieRepository}.
     */
    public MovieRepository() {
    }

    /**
     * Loads a list of movies from TheMovieDB, including their basic information.
     *
     * @param context  the context to get the api key string
     * @param page     the page of movies to load
     * @param sort     the sorting scheme to decide which movies to load
     * @param listener the callback for when the load finished or failed
     */
    public void getMoviesOnline(@NonNull Context context, int page, @NonNull String sort,
                                @NonNull final GetMoviesOnlineListener listener) {
        mLoadMovies = MovieDbClient.getService().loadMoviePosters(page, sort,
                context.getString(R.string.movie_db_key));
        mLoadMovies.enqueue(new Callback<MoviesPage>() {
            @Override
            public void onResponse(Response<MoviesPage> response, Retrofit retrofit) {
                MoviesPage moviesPage = response.body();
                if (moviesPage != null) {
                    listener.onMoviesOnlineLoaded(moviesPage.getMovies());
                } else {
                    listener.onMoviesOnlineLoadFailed();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                listener.onMoviesOnlineLoadFailed();
            }
        });
    }

    /**
     * Loads the detail information of a movie from TheMovieDB.
     *
     * @param context   the context to get the api key string
     * @param movieDbId the db id of the movie
     * @param listener  the callback for when the load finished or failed
     */
    public void getMovieDetailsOnline(@NonNull Context context, int movieDbId,
                                      @NonNull final GetMovieDetailsOnlineListener listener) {
        mLoadMovieDetails = MovieDbClient.getService().loadMovieDetails(movieDbId,
                context.getString(R.string.movie_db_key), "reviews,videos");
        mLoadMovieDetails.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(Response<MovieDetails> response, Retrofit retrofit) {
                MovieDetails movieDetails = response.body();
                if (movieDetails != null) {
                    listener.onMovieDetailsOnlineLoaded(movieDetails);
                } else {
                    listener.onMovieDetailsOnlineLoadFailed();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                listener.onMovieDetailsOnlineLoadFailed();
            }
        });
    }

    /**
     * Cancel all running online load operations.
     */
    public void cancelOnlineLoad() {
        if (mLoadMovies != null) {
            mLoadMovies.cancel();
        }

        if (mLoadMovieDetails != null) {
            mLoadMovieDetails.cancel();
        }
    }

    /**
     * Returns a {@link CursorLoader} that loads the favourite movies of the user.
     *
     * @param context the context to use in the loader
     * @return a {@link CursorLoader} that loads the favourite movies of the user
     */
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

    /**
     * Returns the db id of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the db id of a user's favourite movie
     */
    public int getMovieDbIdFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getInt(MovieRepository.COL_INDEX_MOVIE_DB_ID);
    }

    /**
     * Returns the title of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the title of a user's favourite movie
     */
    public String getMovieTitleFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getString(MovieRepository.COL_INDEX_MOVIE_TITLE);
    }

    /**
     * Returns the release date of a user's favourite movie via the data of the cursor. Must be a
     * cursor obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the release date a user's favourite movie
     */
    public Date getMovieReleaseDateFromFavMoviesCursor(@NonNull Cursor cursor) {
        return new Date(cursor.getLong(MovieRepository.COL_INDEX_MOVIE_RELEASE_DATE));
    }

    /**
     * Returns the poster of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the poster a user's favourite movie
     */
    public String getMoviePosterFromFavMoviesCursor(@NonNull Cursor cursor) {
        return cursor.getString(MovieRepository.COL_INDEX_MOVIE_POSTER);
    }

    /**
     * Returns a {@link CursorLoader} that loads the details of a user's favourite movie.
     *
     * @param context    the context to use in the loader
     * @param movieRowId the row id of the movie
     * @return Returns a {@link CursorLoader} that loads the details of a user's favourite movie
     */
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

    /**
     * Returns a new {@link Movie} object from the data in the cursor. Must be a cursor obtained
     * via the {@link #getFavMovieDetailsLoader(Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return a new {@link Movie} object
     */
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

        return new Movie(videos, backdrop, dbId, plot, releaseDate, poster, title, rating, reviews);
    }

    /**
     * Returns a {@link CursorLoader} that loads only the row id of a movie to check if it exists in
     * the database or not.
     *
     * @param context   the context to use in the loader
     * @param movieDbId the db id of the movie to load
     * @return a {@link CursorLoader} that loads only the row id of a movie
     */
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

    /**
     * Returns the row id of the movie via the data of the cursor. Must be a cursor obtained via
     * the {@link #getIsFavLoader(Context, int)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the row id of the movie
     */
    public long getRowIdFromIsFavCursor(@NonNull Cursor cursor) {
        return cursor.getLong(0);
    }

    /**
     * Inserts a {@link Movie} into the local content provider.
     *
     * @param context  the context to get the {@link ContentResolver}
     * @param movie    the movie to insert
     * @param listener the callback for when the insert finished
     */
    public void insertMovieLocal(@NonNull Context context, @NonNull Movie movie,
                                 @NonNull LocalOperationsListener listener) {
        final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        mInsertMovieTask = new InsertMovieTask(contentResolver, listener);
        mInsertMovieTask.execute(movie);
    }

    /**
     * Deletes a movie from the local content provider.
     *
     * @param context    the context to get the {@link ContentResolver}
     * @param movieRowId the row id of the movie to delete
     * @param listener   the callback for when the delete finished
     */
    public void deleteMovieLocal(@NonNull Context context, long movieRowId,
                                 @NonNull LocalOperationsListener listener) {
        final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        mDeleteMovieHandler = new DeleteMovieHandler(contentResolver, listener);
        mDeleteMovieHandler.startDelete(
                TOKEN_DELETE,
                null,
                MovieContract.Movie.buildMovieUri(movieRowId),
                null,
                null
        );
    }

    /**
     * Update a movie from the local content provider with new data fetched online.
     *
     * @param context      the context to get the {@link ContentResolver}
     * @param movieDetails the new online data
     * @param movieRowId   the row id of the movie to update
     * @param listener     the callback for when the update finished
     */
    public void updateMovieLocal(@NonNull Context context, @NonNull MovieDetails movieDetails,
                                 long movieRowId, @NonNull LocalOperationsListener listener) {
        final ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        mUpdateMovieTask = new UpdateMovieTask(contentResolver, movieRowId, listener);
        mUpdateMovieTask.execute(movieDetails);
    }

    /**
     * Cancels all local content provider operations.
     */
    public void cancelLocalOperations() {
        if (mInsertMovieTask != null) {
            mInsertMovieTask.cancel(true);
        }

        if (mUpdateMovieTask != null) {
            mUpdateMovieTask.cancel(true);
        }

        if (mDeleteMovieHandler != null) {
            mDeleteMovieHandler.cancelOperation(TOKEN_DELETE);
        }
    }

    /**
     * Provides callbacks for the load movies online method.
     */
    public interface GetMoviesOnlineListener {
        /**
         * Called when the online load finished successfully.
         *
         * @param movies the loaded movies
         */
        void onMoviesOnlineLoaded(@NonNull List<Movie> movies);

        /**
         * Called when the online load failed.
         */
        void onMoviesOnlineLoadFailed();
    }

    /**
     * Provides callbacks for the load movies details online method.
     */
    public interface GetMovieDetailsOnlineListener {
        /**
         * Called when the online load finished successfully.
         *
         * @param movieDetails the loaded movie details
         */
        void onMovieDetailsOnlineLoaded(@NonNull MovieDetails movieDetails);

        /**
         * Called when the online load failed.
         */
        void onMovieDetailsOnlineLoadFailed();
    }

    /**
     * Provides callbacks for local content provider operations.
     */
    public interface LocalOperationsListener {
        /**
         * Called when a movie was inserted into the local content provider.
         */
        void onMovieInserted();

        /**
         * Called when a movie was deleted from the local content provider.
         */
        void onMovieDeleted();

        /**
         * Called when a movie was updated in the local content provider.
         */
        void onMovieUpdated();

        /**
         * Called when a local content provider operation fails.
         */
        void onLocalOperationFailed();
    }

    /**
     * Handles content provider batch insert operation on a background thread. To avoid a leak, the
     * process needs to be canceled in the activity's or fragment's onPause() method.
     */
    private class InsertMovieTask extends AsyncTask<Movie, Integer, ContentProviderResult[]> {

        private ContentResolver mContentResolver;
        private LocalOperationsListener mListener;

        public InsertMovieTask(@NonNull ContentResolver contentResolver,
                               @NonNull LocalOperationsListener listener) {
            mContentResolver = contentResolver;
            mListener = listener;
        }

        @Override
        protected ContentProviderResult[] doInBackground(Movie... params) {
            final Movie movie = params[0];

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

            try {
                return mContentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ContentProviderResult[] contentProviderResults) {
            super.onPostExecute(contentProviderResults);

            if (contentProviderResults != null) {
                mListener.onMovieInserted();
            } else {
                mListener.onLocalOperationFailed();
            }
        }
    }

    /**
     * Handles content provider batch update operations on a background thread. To avoid a leak, the
     * process needs to be canceled in the activity's or fragment's onPause() method.
     */
    private class UpdateMovieTask extends AsyncTask<MovieDetails, Integer, ContentProviderResult[]> {

        private ContentResolver mContentResolver;
        private LocalOperationsListener mListener;
        private long mMovieRowId;

        public UpdateMovieTask(@NonNull ContentResolver contentResolver, long movieRowId,
                               @NonNull LocalOperationsListener listener) {
            mContentResolver = contentResolver;
            mMovieRowId = movieRowId;
            mListener = listener;
        }

        @Override
        protected ContentProviderResult[] doInBackground(MovieDetails... params) {
            final MovieDetails movieDetails = params[0];
            final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            updateMovie(movieDetails, ops);
            updateReviews(movieDetails, ops);
            updateVideos(movieDetails, ops);

            try {
                return mContentResolver.applyBatch(MovieContract.CONTENT_AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, e.toString());
                return null;
            }
        }

        private void updateMovie(@NonNull MovieDetails movieDetails,
                                 @NonNull ArrayList<ContentProviderOperation> ops) {
            ops.add(ContentProviderOperation
                            .newUpdate(MovieContract.Movie.buildMovieUri(mMovieRowId))
                            .withValues(movieDetails.getContentValuesEntry())
                            .build()
            );
        }

        private void updateReviews(@NonNull MovieDetails movieDetails,
                                   @NonNull ArrayList<ContentProviderOperation> ops) {
            ops.add(ContentProviderOperation
                            .newDelete(MovieContract.Review.buildReviewsFromMovieUri(mMovieRowId))
                            .build()
            );

            List<Review> reviews = movieDetails.getReviewsPage().getReviews();
            if (!reviews.isEmpty()) {
                for (Review review : reviews) {
                    ops.add(ContentProviderOperation
                                    .newInsert(MovieContract.Review.CONTENT_URI)
                                    .withValue(MovieContract.Review.COLUMN_MOVIE_ID, mMovieRowId)
                                    .withValues(review.getContentValuesEntry())
                                    .build()
                    );
                }
            }
        }

        private void updateVideos(@NonNull MovieDetails movieDetails,
                                  @NonNull ArrayList<ContentProviderOperation> ops) {
            ops.add(ContentProviderOperation
                            .newDelete(MovieContract.Video.buildVideosFromMovieUri(mMovieRowId))
                            .build()
            );

            List<Video> videos = movieDetails.getVideosPage().getVideos();
            if (!videos.isEmpty()) {
                for (Video video : videos) {
                    // only add youtube videos
                    if (video.siteIsYouTube()) {
                        ops.add(ContentProviderOperation
                                        .newInsert(MovieContract.Video.CONTENT_URI)
                                        .withValue(MovieContract.Video.COLUMN_MOVIE_ID, mMovieRowId)
                                        .withValues(video.getContentValuesEntry())
                                        .build()
                        );
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(ContentProviderResult[] contentProviderResults) {
            super.onPostExecute(contentProviderResults);

            if (contentProviderResults != null) {
                mListener.onMovieUpdated();
            } else {
                mListener.onLocalOperationFailed();
            }
        }
    }

    /**
     * Handles content provider delete operation on a background thread. To avoid a leak, the
     * process needs to be canceled in the activity's or fragment's onPause() method.
     */
    @SuppressWarnings("HandlerLeak")
    private class DeleteMovieHandler extends AsyncQueryHandler {

        private LocalOperationsListener mListener;

        public DeleteMovieHandler(ContentResolver cr, LocalOperationsListener listener) {
            super(cr);

            mListener = listener;
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);

            mListener.onMovieDeleted();
        }
    }
}
