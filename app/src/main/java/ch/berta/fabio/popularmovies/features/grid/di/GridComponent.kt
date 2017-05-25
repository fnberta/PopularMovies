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

package ch.berta.fabio.popularmovies.features.grid.di

import android.support.v4.content.CursorLoader
import ch.berta.fabio.popularmovies.di.ApplicationComponent
import ch.berta.fabio.popularmovies.di.scopes.PerScreen
import ch.berta.fabio.popularmovies.features.grid.view.GridActivity
import ch.berta.fabio.popularmovies.features.grid.view.GridFavFragment
import ch.berta.fabio.popularmovies.features.grid.view.GridOnlFragment
import ch.berta.fabio.popularmovies.features.grid.loaders.GridOnlMoviesLoader
import dagger.Component

/**
 * Defines the dependency injection component for the Movie Grid screen.
 */
@PerScreen
@Component(modules = arrayOf(GridLoaderModule::class),
        dependencies = arrayOf(ApplicationComponent::class))
interface GridComponent {

    fun inject(gridActivity: GridActivity)

    fun inject(gridFavFragment: GridFavFragment)

    fun inject(gridOnlFragment: GridOnlFragment)

    val moviesOnlLoader: GridOnlMoviesLoader

    val moviesFavLoader: CursorLoader
}
