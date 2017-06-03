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
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.features.details.view.DetailsActivity
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

const val RQ_DETAILS = 1

data class SelectedMovieWithSort(
        val selectedMovie: SelectedMovie,
        val sort: Sort
)

fun navigationTargets(actions: Observable<GridAction>): Observable<NavigationTarget> {
    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)

    val movieClicks = actions
            .ofType(GridAction.MovieClick::class.java)
            .withLatestFrom(sortSelections,
                    BiFunction<GridAction.MovieClick, GridAction.SortSelection, SelectedMovieWithSort>
                    { (selectedMovie), (sort) -> SelectedMovieWithSort(selectedMovie, sort) })
            .map {
                val args = DetailsArgs(it.selectedMovie.movieId, it.sort.option == SortOption.SORT_FAVORITE)
                NavigationTarget.Activity(DetailsActivity::class.java, args, RQ_DETAILS, it.selectedMovie.posterView,
                        R.string.shared_transition_details_poster)
            }

    val navigationTargets = listOf(movieClicks)
    return Observable.merge(navigationTargets)
}
