/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.domain.repositories;

import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import rx.Observable;

/**
 * Handles the loading of {@link Movie} data from online sources as well as from the locale content
 * provider. Full separation between loading and display is not possible with {@link CursorLoader}
 * and adapters using the cursor instead of POJOs. This is a best effort to provide some kind of
 * separation.
 */
public interface MovieRepository {
    /**
     * Loads a list of movies from TheMovieDB, including their basic information.
     *  @param page    the page of movies to load
     * @param sort    the sorting scheme to decide which movies to load
     */
    Observable<List<Movie>> getMoviesOnline(int page, @NonNull String sort);

    /**
     * Loads the detail information of a movie from TheMovieDB.
     *
     * @param movieDbId the db id of the movie
     */
    Observable<MovieDetails> getMovieDetailsOnline(int movieDbId);

    /**
     * Returns a {@link CursorLoader} that loads the favourite movies of the user.
     *
     * @param context the context to use in the loader
     * @return a {@link CursorLoader} that loads the favourite movies of the user
     */
    CursorLoader getFavMoviesLoader(@NonNull Context context);

    /**
     * Returns the db id of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the db id of a user's favourite movie
     */
    int getMovieDbIdFromFavMoviesCursor(@NonNull Cursor cursor);

    /**
     * Returns the title of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the title of a user's favourite movie
     */
    String getMovieTitleFromFavMoviesCursor(@NonNull Cursor cursor);

    /**
     * Returns the release date of a user's favourite movie via the data of the cursor. Must be a
     * cursor obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the release date a user's favourite movie
     */
    Date getMovieReleaseDateFromFavMoviesCursor(@NonNull Cursor cursor);

    /**
     * Returns the poster of a user's favourite movie via the data of the cursor. Must be a cursor
     * obtained via the {@link #getFavMoviesLoader(Context)} (Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the poster a user's favourite movie
     */
    String getMoviePosterFromFavMoviesCursor(@NonNull Cursor cursor);

    /**
     * Returns a {@link CursorLoader} that loads the details of a user's favourite movie.
     *
     * @param context    the context to use in the loader
     * @param movieRowId the row id of the movie
     * @return Returns a {@link CursorLoader} that loads the details of a user's favourite movie
     */
    CursorLoader getFavMovieDetailsLoader(@NonNull Context context, long movieRowId);

    /**
     * Returns a new {@link Movie} object from the data in the cursor. Must be a cursor obtained
     * via the {@link #getFavMovieDetailsLoader(Context, long)} method.
     *
     * @param cursor the cursor to get the data from
     * @return a new {@link Movie} object
     */
    Movie getMovieFromFavMovieDetailsCursor(@NonNull Cursor cursor);

    /**
     * Returns a {@link CursorLoader} that loads only the row id of a movie to check if it exists in
     * the database or not.
     *
     * @param context   the context to use in the loader
     * @param movieDbId the db id of the movie to load
     * @return a {@link CursorLoader} that loads only the row id of a movie
     */
    CursorLoader getIsFavLoader(@NonNull Context context, int movieDbId);

    /**
     * Returns the row id of the movie via the data of the cursor. Must be a cursor obtained via
     * the {@link #getIsFavLoader(Context, int)} method.
     *
     * @param cursor the cursor to get the data from
     * @return the row id of the movie
     */
    long getRowIdFromIsFavCursor(@NonNull Cursor cursor);

    /**
     * Inserts a {@link Movie} into the local content provider.
     *
     * @param movie   the movie to insert
     */
    Observable<ContentProviderResult[]> insertMovieLocal(@NonNull Movie movie);

    /**
     * Deletes a movie from the local content provider.
     *
     * @param movieRowId the row id of the movie to delete
     */
    Observable<Integer> deleteMovieLocal(long movieRowId);

    /**
     * Update a movie from the local content provider with new data fetched online.
     *  @param movieDetails the new online data
     * @param movieRowId   the row id of the movie to update
     */
    Observable<ContentProviderResult[]> updateMovieLocal(@NonNull MovieDetails movieDetails,
                                                         long movieRowId);
}
