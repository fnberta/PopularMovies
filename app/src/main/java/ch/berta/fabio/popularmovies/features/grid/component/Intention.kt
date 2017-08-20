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

import ch.berta.fabio.popularmovies.features.details.view.RQ_DETAILS
import ch.berta.fabio.popularmovies.features.details.view.RS_DELETED_FROM_FAV
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortSelectionState
import io.reactivex.Observable

fun intention(sources: GridSources, sortOptions: List<Sort>): Observable<GridAction> {
    val transientClears = sources.uiEvents.transientClears.map { GridAction.TransientClear }

    val sortSelectionsSharedPrefs = sources.sharedPrefs.getSortPos()
            .map { SortSelectionState(sortOptions[it], sortOptions[0]) }
    val sortSelectionsSpinner = sources.uiEvents.sortSelections
            .skip(1) // skip initial position 0 emission (spinner always emit this)
            .map { sortOptions[it] }
            .scan(SortSelectionState(sortOptions[0], sortOptions[0]),
                    { (sort), curr -> SortSelectionState(curr, sort) })
            .skip(1) // skip initial scan emission

    val sortSelections = Observable.merge(sortSelectionsSharedPrefs, sortSelectionsSpinner)
            .distinctUntilChanged()
            .map { GridAction.SortSelection(it.sort, it.sortPrev) }
    val movieClicks = sources.uiEvents.movieSelections.map { GridAction.MovieSelection(it) }
    val loadMore = sources.uiEvents.loadMore
            .map { 1 }
            .scan(1, { acc, curr -> acc + curr })
            .skip(1) // skip initial scan emission
            .map { GridAction.LoadMore(it) }
    val refreshSwipes = sources.uiEvents.refreshSwipes.map { GridAction.RefreshSwipe }

    val favDelete = sources.uiEvents.activityResults
            .filter { it.requestCode == RQ_DETAILS && it.resultCode == RS_DELETED_FROM_FAV }
            .map { GridAction.MovieFavDeleted }

    val actions = listOf(transientClears, sortSelections, movieClicks, loadMore, refreshSwipes, favDelete)
    return Observable.merge(actions)
}