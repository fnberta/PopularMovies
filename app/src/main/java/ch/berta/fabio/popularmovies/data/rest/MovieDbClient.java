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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Provides a singleton {@link Retrofit} instance that queries TheMovieDB for movies.
 */
public class MovieDbClient {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final String DATE_FORMAT = "yyyy-mm-dd";
    private static final Retrofit REST_ADAPTER = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(getGsonObject()))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();
    private static final PopularMovies POPULAR_MOVIES_SERVICE =
            REST_ADAPTER.create(PopularMovies.class);

    private MovieDbClient() {
        // class cannot be instantiated
    }

    /**
     * Returns the singleton instance of the {@link Retrofit} adapter.
     *
     * @return the {@link Retrofit} adapter
     */
    public static PopularMovies getService() {
        return POPULAR_MOVIES_SERVICE;
    }

    /**
     * Returns a custom date deserializer that handles empty strings and returns today's date instead.
     *
     * @return the Gson object to use
     */
    private static Gson getGsonObject() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);

            @Override
            public Date deserialize(final JsonElement json, final Type typeOfT,
                                    final JsonDeserializationContext context) throws JsonParseException {
                try {
                    return dateFormat.parse(json.getAsString());
                } catch (ParseException e) {
                    return new Date();
                }
            }
        });

        return gsonBuilder.create();
    }

    /**
     * Provides the query calls to the TheMovieDB.
     */
    public interface PopularMovies {
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
}
