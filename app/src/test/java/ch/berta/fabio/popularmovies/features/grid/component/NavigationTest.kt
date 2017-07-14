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

import ch.berta.fabio.popularmovies.ImmediateSchedulersRule
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.features.details.component.DetailsAction
import ch.berta.fabio.popularmovies.features.details.view.DetailsActivity
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.makeSortOptions
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import com.jakewharton.rxrelay2.PublishRelay
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    /*
    * 0: by popularity
    * 1: by rating
    * 2: by release date
    * 3: favorites
    */
    val sortOptions = makeSortOptions { "someRandomTitle" }
    val actions: PublishRelay<GridAction> = PublishRelay.create()

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Test
    fun shouldNavigateToToDetailsScreenOnMovieClick() {
        val navigation = navigationTargets(actions)
        val observer = navigation.test()

        val sortPop = sortOptions[0]
        val sortFav = sortOptions[3]
        val selectedMovie = SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", null)

        val expectedNavigationTargets = listOf(
                NavigationTarget.Activity(DetailsActivity::class.java,
                        DetailsArgs(selectedMovie.id, selectedMovie.title, selectedMovie.releaseDate,
                                selectedMovie.overview, selectedMovie.voteAverage, selectedMovie.poster,
                                selectedMovie.backdrop, false),
                        RQ_DETAILS,
                        selectedMovie.posterView,
                        R.string.shared_transition_details_poster
                ),
                NavigationTarget.Activity(DetailsActivity::class.java,
                        DetailsArgs(selectedMovie.id, selectedMovie.title, selectedMovie.releaseDate,
                                selectedMovie.overview, selectedMovie.voteAverage, selectedMovie.poster,
                                selectedMovie.backdrop, true),
                        RQ_DETAILS,
                        selectedMovie.posterView,
                        R.string.shared_transition_details_poster
                )
        )

        actions.accept(GridAction.SortSelection(sortPop, sortPop))
        actions.accept(GridAction.MovieClick(selectedMovie))
        actions.accept(GridAction.SortSelection(sortFav, sortPop))
        actions.accept(GridAction.MovieClick(selectedMovie))

        observer.assertValues(*expectedNavigationTargets.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }
}