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

package ch.berta.fabio.popularmovies.features.details.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.component.*
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

class DetailsViewModel(
        movieStorage: MovieStorage,
        detailsArgs: DetailsArgs
) : ViewModel() {

    val state: LiveData<DetailsState>
    val navigation: Observable<NavigationTarget>

    val uiEvents = DetailsUiEvents()

    init {
        val sources = DetailsSources(uiEvents, movieStorage)
        val sinks = main(sources, detailsArgs)

        state = LiveDataReactiveStreams.fromPublisher(sinks
                .ofType(DetailsSink.State::class.java)
                .map { it.state }
                .toFlowable(BackpressureStrategy.LATEST)
        )
        navigation = sinks
                .ofType(DetailsSink.Navigation::class.java)
                .map { it.target }
    }
}