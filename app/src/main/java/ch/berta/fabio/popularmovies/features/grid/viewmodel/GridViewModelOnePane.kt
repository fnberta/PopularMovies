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
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.component.GridSources
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.component.GridUiEvents
import ch.berta.fabio.popularmovies.features.grid.component.main
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.toLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

fun getGridState(
        uiEvents: GridUiEvents,
        sharedPrefs: SharedPrefs,
        movieStorage: MovieStorage,
        sortOptions: List<Sort>
): Observable<GridState> {
    val sources = GridSources(uiEvents, sharedPrefs, movieStorage)
    val initialState = GridState(sortOptions[0])
    return main(sources, initialState, sortOptions)
}

class GridViewModelOnePane(
        sharedPrefs: SharedPrefs,
        movieStorage: MovieStorage,
        sortOptions: List<Sort>
) : ViewModel(), GridViewModel {

    override val transientClears: PublishRelay<Unit> = PublishRelay.create()
    override val activityResults: PublishRelay<ActivityResult> = PublishRelay.create()
    override val sortSelections: PublishRelay<Int> = PublishRelay.create()
    override val movieSelections: PublishRelay<SelectedMovie> = PublishRelay.create()
    override val loadMore: PublishRelay<Unit> = PublishRelay.create()
    override val refreshSwipes: PublishRelay<Unit> = PublishRelay.create()

    override val state: LiveData<MoviesState>

    init {
        val uiEvents = GridUiEvents(transientClears, activityResults, sortSelections, movieSelections, loadMore,
                refreshSwipes)
        state = getGridState(uiEvents, sharedPrefs, movieStorage, sortOptions)
                .map<MoviesState> { MoviesState.Grid(it) }
                .toLiveData()
    }
}