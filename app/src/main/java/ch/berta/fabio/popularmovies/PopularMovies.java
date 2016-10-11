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

package ch.berta.fabio.popularmovies;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import ch.berta.fabio.popularmovies.di.ApplicationComponent;
import ch.berta.fabio.popularmovies.di.ApplicationModule;
import ch.berta.fabio.popularmovies.di.DaggerApplicationComponent;
import ch.berta.fabio.popularmovies.di.MovieServiceModule;

/**
 * Provides state across the lifecycle of the whole application. Used to build the AppComponent
 * Dagger component.
 * <p/>
 * Subclass of {@link Application}.
 */
public class PopularMovies extends Application {

    private ApplicationComponent mAppComponent;

    public static ApplicationComponent getAppComponent(@NonNull Activity activity) {
        return ((PopularMovies) activity.getApplication()).getAppComponent();
    }

    public ApplicationComponent getAppComponent() {
        return mAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .movieServiceModule(new MovieServiceModule())
                .build();
    }
}
