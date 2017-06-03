/*
 * Copyright (c) 2017 Fabio Berta
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

package ch.berta.fabio.popularmovies.data.themoviedb

import ch.berta.fabio.popularmovies.BuildConfig
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MovieInfo
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MoviesPage
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Provides the query calls to the TheMovieDB.
 */
interface TheMovieDbService {
    /**
     * Queries TheMovieDB for movies based on the page number and sort value.
     *
     * @param page   the page to query
     * @param sortBy the value to sort movies by
     * @param apiKey the api key for querying TheMovieDB.
     * @return a [Single] with the query
     */
    @GET("discover/movie")
    fun loadMovies(
            @Query("page") page: Int,
            @Query("sort_by") sortBy: String,
            @Query("api_key") apiKey: String = BuildConfig.MOVIE_DB_API_KEY
    ): Single<MoviesPage>

    /**
     * Queries TheMovieDB for movie details.
     *
     * @param movieId  the db id of the movie
     * @param apiKey   the api key for querying TheMovieDB.
     * @param appendTo the extra query information to append
     * @return a [Single] with the query
     */
    @GET("movie/{id}")
    fun loadMovieDetails(
            @Path("id") movieId: Int,
            @Query("append_to_response") appendTo: String = "reviews,videos",
            @Query("api_key") apiKey: String = BuildConfig.MOVIE_DB_API_KEY
    ): Single<MovieInfo>
}
