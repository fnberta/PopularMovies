/*
 * Copyright (c) 2015 Fabio Berta
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
 * Created by fabio on 03.10.15.
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

    public static MoviePosters getService() {
        return MOVIE_POSTERS_SERVICE;
    }

    private static Gson getGsonObject() {
        return new GsonBuilder().setDateFormat("yyyy-mm-dd").create();
    }

    public interface MoviePosters {
        @GET("discover/movie")
        Call<MoviesPage> loadMoviePosters(@Query("page") int page, @Query("sort_by") String sortBy,
                                          @Query("api_key") String apiKey);
    }
}
