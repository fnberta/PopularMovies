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

package ch.berta.fabio.popularmovies.features.grid.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.component.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class GridViewModel(
        sharedPrefs: SharedPrefs,
        movieStorage: MovieStorage,
        sortOptions: List<Sort>
) : ViewModel() {

    val state: LiveData<GridState>
    val navigation: Observable<NavigationTarget>

    val uiEvents = GridUiEvents()
    val activityResults: PublishRelay<ActivityResult> = PublishRelay.create()
    val localDbWriteResults: PublishRelay<LocalDbWriteResult> = PublishRelay.create()

    val disposable: CompositeDisposable

    init {
        val sources = GridSources(uiEvents, activityResults, sharedPrefs, movieStorage, localDbWriteResults)
        val sinks = main(sources, sortOptions)

        disposable = initWriteEffectsSubscriptions(sinks)
        state = LiveDataReactiveStreams.fromPublisher(sinks
                .ofType(GridSink.State::class.java)
                .map { it.state }
                .toFlowable(BackpressureStrategy.LATEST)
        )
        navigation = sinks
                .ofType(GridSink.Navigation::class.java)
                .map { it.target }
    }

    private fun initWriteEffectsSubscriptions(sinks: Observable<GridSink>): CompositeDisposable =
            CompositeDisposable().apply {
                add(sinks
                        .ofType(GridSink.SharedPrefsWrite::class.java)
                        .subscribe())
                add(sinks
                        .ofType(GridSink.LocalDbWrite::class.java)
                        .map { it.result }
                        .subscribe(localDbWriteResults))
            }

    override fun onCleared() {
        super.onCleared()

        disposable.clear()
    }
}