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

package ch.berta.fabio.popularmovies.di.modules;

import android.support.annotation.NonNull;

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

import javax.inject.Singleton;

import ch.berta.fabio.popularmovies.data.rest.MovieService;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Defines the instantiation of the query movies service.
 */
@Module
public class MovieServiceModule {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final String DATE_FORMAT = "yyyy-mm-dd";

    @Provides
    @Singleton
    Gson providesGson() {
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

    @Provides
    @Singleton
    MovieService provideMoviesService(@NonNull Gson gson) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(MovieService.class);
    }
}
