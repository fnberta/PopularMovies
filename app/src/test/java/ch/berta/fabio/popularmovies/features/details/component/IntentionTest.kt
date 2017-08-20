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

package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.ImmediateSchedulersRule
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.dtos.YOU_TUBE
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import com.jakewharton.rxrelay2.PublishRelay
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class IntentionTest {

    private val movieStorage = MovieStorage(Mockito.mock(TheMovieDbService::class.java),
            Mockito.mock(MovieDb::class.java))

    private val transientClears: PublishRelay<Unit> = PublishRelay.create()
    private val movieSelections: PublishRelay<SelectedMovie> = PublishRelay.create()
    private val updateSwipes: PublishRelay<Unit> = PublishRelay.create()
    private val favClicks: PublishRelay<Unit> = PublishRelay.create()
    private val videoClicks: PublishRelay<DetailsVideoRowViewData> = PublishRelay.create()
    private val uiEvents = DetailsUiEvents(transientClears, movieSelections, updateSwipes, favClicks, videoClicks)
    private val sources = DetailsSources(uiEvents, movieStorage)

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Test
    fun shouldMapMovieSelectionsCorrectly() {
        val intentions = intention(sources)
        val observer = intentions.test()

        val selectedMovie = SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", false)
        val expectedActions = listOf(DetailsAction.MovieSelected(selectedMovie))

        movieSelections.accept(selectedMovie)
        movieSelections.accept(selectedMovie) // same emission should be ignored

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapFavClicksCorrectly() {
        val intentions = intention(sources)
        val observer = intentions.test()

        val expectedActions = listOf(DetailsAction.FavClick)

        favClicks.accept(Unit)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapUpdateSwipesCorrectly() {
        val intentions = intention(sources)
        val observer = intentions.test()

        val expectedActions = listOf(DetailsAction.UpdateSwipe)

        updateSwipes.accept(Unit)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldMapVideoClicksCorrectly() {
        val intentions = intention(sources)
        val observer = intentions.test()

        val video1 = DetailsVideoRowViewData("key", "name", "some-site", 10)
        val video2 = DetailsVideoRowViewData("key", "name", YOU_TUBE, 10)
        val expectedActions = listOf(DetailsAction.VideoClick(video2))

        videoClicks.accept(video1) // site not youtube, should be ignored
        videoClicks.accept(video2)

        observer.assertValues(*expectedActions.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }
}