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

import android.support.annotation.StringRes
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.log
import io.reactivex.Observable

sealed class GridAction {
    object TransientClear : GridAction()
    data class SortSelection(val sort: Sort, val sortPrev: Sort) : GridAction()
    data class MovieSelection(val selectedMovie: SelectedMovie) : GridAction()
    data class LoadMore(val page: Int) : GridAction()
    object RefreshSwipe : GridAction()
    object MovieFavDeleted : GridAction()
}

data class GridSources(
        val uiEvents: GridUiEvents,
        val sharedPrefs: SharedPrefs,
        val movieStorage: MovieStorage
)

data class GridUiEvents(
        val transientClears: Observable<Unit>,
        val activityResults: Observable<ActivityResult>,
        val sortSelections: Observable<Int>,
        val movieSelections: Observable<SelectedMovie>,
        val loadMore: Observable<Unit>,
        val refreshSwipes: Observable<Unit>
)

data class GridState(
        val sort: Sort,
        val movies: List<GridRowViewData> = emptyList(),
        val empty: Boolean = false,
        val loading: Boolean = false,
        val loadingMore: Boolean = false,
        val refreshing: Boolean = false,
        @StringRes val message: Int? = null,
        val selectedMovie: SelectedMovie? = null
)

fun main(sources: GridSources, initialState: GridState, sortOptions: List<Sort>): Observable<GridState> =
        intention(sources, sortOptions)
                .log("grid action")
                .publish { model(sortOptions, initialState, it, sources.movieStorage, sources.sharedPrefs) }
                .share()
