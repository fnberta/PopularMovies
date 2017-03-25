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

package ch.berta.fabio.popularmovies.di

import ch.berta.fabio.popularmovies.data.services.MovieService
import com.google.gson.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

const val BASE_URL = "http://api.themoviedb.org/3/"
const val DATE_FORMAT = "yyyy-mm-dd"

/**
 * Defines the instantiation of the query movies service.
 */
@Module
class MovieServiceModule {

    @Provides
    @Singleton
    internal fun providesGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
                    private val dateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

                    override fun deserialize(json: JsonElement, typeOfT: Type,
                                             context: JsonDeserializationContext): Date {
                        try {
                            return dateFormat.parse(json.asString)
                        } catch (e: ParseException) {
                            return Date()
                        }

                    }
                })
                .create()
    }

    @Provides
    @Singleton
    internal fun provideMoviesService(gson: Gson): MovieService {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(MovieService::class.java)
    }
}
