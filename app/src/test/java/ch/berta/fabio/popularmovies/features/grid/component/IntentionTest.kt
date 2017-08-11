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

import android.content.Intent
import ch.berta.fabio.popularmovies.ImmediateSchedulersRule
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.details.component.RS_DATA_MOVIE_ID
import ch.berta.fabio.popularmovies.features.details.component.RS_REMOVE_FROM_FAV
import ch.berta.fabio.popularmovies.features.grid.makeSortOptions
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class IntentionTest {

    val sharedPrefs: SharedPrefs = Mockito.mock(SharedPrefs::class.java)
    val movieStorage = MovieStorage(Mockito.mock(TheMovieDbService::class.java), Mockito.mock(MovieDb::class.java))

    /*
    * 0: by popularity
    * 1: by rating
    * 2: by release date
    * 3: favorites
    */
    val sortOptions = makeSortOptions { "someRandomTitle" }
    val activityResults: PublishRelay<ActivityResult> = PublishRelay.create()
    val snackbarShown: PublishRelay<Unit> = PublishRelay.create()
    val sortSelections: PublishRelay<Int> = PublishRelay.create()
    val movieClicks: PublishRelay<SelectedMovie> = PublishRelay.create()
    val loadMore: PublishRelay<Unit> = PublishRelay.create()
    val refreshSwipes: PublishRelay<Unit> = PublishRelay.create()
    val uiEvents = GridUiEvents(activityResults, snackbarShown, sortSelections, movieClicks, loadMore, refreshSwipes)
    val sources = GridSources(uiEvents, sharedPrefs, movieStorage)

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun setUp() {
        Mockito.`when`(sharedPrefs.getSortPos()).thenReturn(Observable.just(0))
    }

    @Test
    fun shouldMapSortSelectionToSelectionWithPrevious() {
        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[0], sortOptions[0]),
                GridAction.SortSelection(sortOptions[1], sortOptions[0]),
                GridAction.SortSelection(sortOptions[2], sortOptions[1])
        )

        sortSelections.accept(0) // imitate spinner behaviour, emit 0 emission even if nothing is selected yet
        sortSelections.accept(0)
        sortSelections.accept(0) // same value emission should be ignored
        sortSelections.accept(1)
        sortSelections.accept(2)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldStartWithSortFromSavedPrefs() {
        Mockito.`when`(sharedPrefs.getSortPos()).thenReturn(Observable.just(1))

        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[1], sortOptions[0])
        )

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapMovieClick() {
        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val selectedMovie = SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", null)
        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[0], sortOptions[0]),
                GridAction.MovieClick(selectedMovie)
        )

        movieClicks.accept(selectedMovie)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapScrollToCorrectNextPage() {
        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[0], sortOptions[0]),
                GridAction.LoadMore(2),
                GridAction.LoadMore(3),
                GridAction.LoadMore(4)
        )

        loadMore.accept(Unit)
        loadMore.accept(Unit)
        loadMore.accept(Unit)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapRefreshSwipe() {
        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[0], sortOptions[0]),
                GridAction.RefreshSwipe
        )

        refreshSwipes.accept(Unit)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapActivityResultsCorrectly() {
        val intent = Mockito.mock(Intent::class.java)
        Mockito.`when`(intent.putExtra(RS_DATA_MOVIE_ID, 0)).thenReturn(intent)
        Mockito.`when`(intent.getIntExtra(RS_DATA_MOVIE_ID, -1)).thenReturn(0)

        val intentions = intention(sources, sortOptions)
        val observer = intentions.test()

        val expectedActions = listOf(
                GridAction.SortSelection(sortOptions[0], sortOptions[0]),
                GridAction.FavDelete(0)
        )

        activityResults.accept(ActivityResult(RQ_DETAILS, RS_REMOVE_FROM_FAV, intent))
        activityResults.accept(
                ActivityResult(RQ_DETAILS, RS_REMOVE_FROM_FAV, null)) // data is null, should be ignored
        activityResults.accept(
                ActivityResult(0, RS_REMOVE_FROM_FAV, intent)) // other reqCode, should be ignored
        activityResults.accept(ActivityResult(RQ_DETAILS, 2, intent)) // other resCode, should be ignored
        Mockito.`when`(intent.getIntExtra(RS_DATA_MOVIE_ID, -1)).thenReturn(-1)
        activityResults.accept(
                ActivityResult(RQ_DETAILS, RS_REMOVE_FROM_FAV, intent)) // intent returns -1, should be ignored

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }
}