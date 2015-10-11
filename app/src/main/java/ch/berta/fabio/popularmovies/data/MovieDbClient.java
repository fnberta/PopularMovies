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

package ch.berta.fabio.popularmovies.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Provides a singleton {@link Retrofit} instance that queries TheMovieDB for movies.
 */
public class MovieDbClient {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final Retrofit REST_ADAPTER = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(getGsonObject()))
            .build();
    private static final MoviePosters MOVIE_POSTERS_SERVICE =
            REST_ADAPTER.create(MoviePosters.class);

    private MovieDbClient() {
        // class cannot be instantiated
    }

    /**
     * Returns the singleton instance of the {@link Retrofit} adapter.
     *
     * @return the {@link Retrofit} adapter
     */
    public static MoviePosters getService() {
        return MOVIE_POSTERS_SERVICE;
    }

    private static Gson getGsonObject() {
        return new GsonBuilder().setDateFormat("yyyy-mm-dd").create();
    }

    /**
     * Provides the query calls to the TheMovieDB.
     */
    public interface MoviePosters {
        /**
         * Queries TheMovieDB for movies based on the page number and sort option.
         *
         * @param page   the page to query
         * @param sortBy the option to sort movies by
         * @param apiKey the api key for querying TheMovieDB.
         * @return a {@link Call} object with the query
         */
        @GET("discover/movie")
        Call<MoviesPage> loadMoviePosters(@Query("page") int page, @Query("sort_by") String sortBy,
                                          @Query("api_key") String apiKey);
    }
}
