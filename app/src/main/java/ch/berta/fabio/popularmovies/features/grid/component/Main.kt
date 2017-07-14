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

package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

data class GridSources(
        val uiEvents: GridUiEvents,
        val sharedPrefs: SharedPrefs,
        val movieStorage: MovieStorage
)

data class GridUiEvents(
        val activityResults: PublishRelay<ActivityResult> = PublishRelay.create(),
        val snackbarShown: PublishRelay<Unit> = PublishRelay.create(),
        val sortSelections: PublishRelay<Int> = PublishRelay.create(),
        val movieClicks: PublishRelay<SelectedMovie> = PublishRelay.create(),
        val loadMore: PublishRelay<Unit> = PublishRelay.create(),
        val refreshSwipes: PublishRelay<Unit> = PublishRelay.create()
)

sealed class GridAction {
    object SnackbarShown : GridAction()
    data class SortSelection(val sort: Sort, val sortPrev: Sort) : GridAction()
    data class MovieClick(val selectedMovie: SelectedMovie) : GridAction()
    data class LoadMore(val page: Int) : GridAction()
    object RefreshSwipe : GridAction()
    data class FavDelete(val movieId: Int) : GridAction()
}

sealed class GridSink {
    data class State(val state: GridState) : GridSink()
    data class Navigation(val target: NavigationTarget) : GridSink()
}

fun main(
        sources: GridSources,
        initialState: GridState,
        sortOptions: List<Sort>
): Observable<GridSink> = intention(sources, sortOptions)
        .log("action")
        .publish {
            val state = model(sortOptions, initialState, it, sources.movieStorage, sources.sharedPrefs)
                    .map { GridSink.State(it) }
            val navigationTargets = navigationTargets(it)
                    .map { GridSink.Navigation(it) }

            Observable.merge(state, navigationTargets)
        }
        .share()
