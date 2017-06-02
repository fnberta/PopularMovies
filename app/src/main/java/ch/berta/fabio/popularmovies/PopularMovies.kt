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

package ch.berta.fabio.popularmovies

import android.app.Activity
import android.app.Application
import ch.berta.fabio.popularmovies.di.ApplicationComponent
import ch.berta.fabio.popularmovies.di.DaggerApplicationComponent
import ch.berta.fabio.popularmovies.di.modules.ApplicationModule
import ch.berta.fabio.popularmovies.di.modules.MovieServiceModule
import com.facebook.stetho.Stetho
import timber.log.Timber

/**
 * Provides state across the lifecycle of the whole application. Used to build the AppComponent
 * Dagger component.
 *
 * Subclass of [Application].
 */
class PopularMovies : Application() {

    companion object {
        fun getAppComponent(activity: Activity): ApplicationComponent =
                (activity.application as PopularMovies).appComponent
    }

    private val appComponent by lazy {
        DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .movieServiceModule(MovieServiceModule())
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        }
    }
}
