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

package ch.berta.fabio.popularmovies.di.modules

import android.app.Application
import android.arch.persistence.room.Room
import android.content.SharedPreferences
import android.preference.PreferenceManager
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Defines the instantiation of the singleton targets.
 */
@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    internal fun providesApplication(): Application = application

    @Provides
    @Singleton
    internal fun providesMovieDb(application: Application): MovieDb =
            Room.databaseBuilder(application, MovieDb::class.java, "movies.db").build()

    @Provides
    @Singleton
    internal fun providesSharedPreferences(application: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(application)
}
