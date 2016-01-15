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

package ch.berta.fabio.popularmovies.data.rest;

import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import ch.berta.fabio.popularmovies.domain.models.MoviesPage;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Provides the query calls to the TheMovieDB.
 */
public interface MovieService {
    /**
     * Queries TheMovieDB for movies based on the page number and sort option.
     *
     * @param page   the page to query
     * @param sortBy the option to sort movies by
     * @param apiKey the api key for querying TheMovieDB.
     * @return a {@link Call} object with the query
     */
    @GET("discover/movie")
    Observable<MoviesPage> loadMoviePosters(@Query("page") int page,
                                            @Query("sort_by") String sortBy,
                                            @Query("api_key") String apiKey);

    /**
     * Queries TheMovieDB for movie details.
     *
     * @param movieId  the db id of the movie
     * @param apiKey   the api key for querying TheMovieDB.
     * @param appendTo the extra query information to append
     * @return a {@link Call} object with the query
     */
    @GET("movie/{id}")
    Observable<MovieDetails> loadMovieDetails(@Path("id") int movieId,
                                              @Query("api_key") String apiKey,
                                              @Query("append_to_response") String appendTo);
}
