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
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.GetMoviesResult
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.data.dtos.Movie
import ch.berta.fabio.popularmovies.features.common.SnackbarMessage
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.makeSortOptions
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowLoadMoreViewData
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowMovieViewData
import ch.berta.fabio.popularmovies.formatLong
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class ModelTest {

    val sharedPrefs: SharedPrefs = Mockito.mock(SharedPrefs::class.java)
    val movieStorage: MovieStorage = Mockito.mock(MovieStorage::class.java)

    /*
    * 0: by popularity
    * 1: by rating
    * 2: by release date
    * 3: favorites
    */
    val sortOptions = makeSortOptions { "someRandomTitle" }
    val initialSort = sortOptions[0]
    val actions: PublishRelay<GridAction> = PublishRelay.create()

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun setUp() {
        Mockito.`when`(sharedPrefs.writeSortPos(Mockito.anyInt())).thenReturn(Observable.just(Unit))
    }

    @Test
    fun shouldLoadFavMoviesOnSelection() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        // expected grid state result emissions
        val favMovies = getMovies(3)
        Mockito.`when`(movieStorage.getFavMovies()).thenReturn(Observable.just(GetMoviesResult.Success(favMovies)))
        val gridViewData = mapGridRowMovieViewData(favMovies)
        val expectedStates = listOf(
                GridState(sort = sortOptions[3], loading = true),
                GridState(sort = sortOptions[3], movies = gridViewData, loading = false)
        )

        actions.accept(GridAction.SortSelection(sortOptions[3], initialSort))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldLoadOnlMoviesOnSelection() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val popMovies = getMovies(2)
        Mockito.`when`(movieStorage.getOnlMovies(1, SortOption.SORT_POPULARITY, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(popMovies)))
        val popGridViewData = mapGridRowMovieViewData(popMovies)

        val ratingMovies = getMovies(5)
        Mockito.`when`(movieStorage.getOnlMovies(1, SortOption.SORT_RATING, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(ratingMovies)))
        val ratingGridViewData = mapGridRowMovieViewData(ratingMovies)

        val dateMovies = getMovies(7)
        Mockito.`when`(movieStorage.getOnlMovies(1, SortOption.SORT_RELEASE_DATE, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(dateMovies)))
        val dateGridViewData = mapGridRowMovieViewData(dateMovies)

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(sort = sortOptions[0], movies = popGridViewData, loading = false),
                GridState(sort = sortOptions[1], movies = popGridViewData, loading = true),
                GridState(sort = sortOptions[1], movies = ratingGridViewData, loading = false),
                GridState(sort = sortOptions[2], movies = ratingGridViewData, loading = true),
                GridState(sort = sortOptions[2], movies = dateGridViewData, loading = false)
        )

        actions.accept(GridAction.SortSelection(sortOptions[0], initialSort))
        actions.accept(GridAction.SortSelection(sortOptions[1], sortOptions[0]))
        actions.accept(GridAction.SortSelection(sortOptions[2], sortOptions[1]))
        actions.accept(GridAction.SnackbarShown)

        Mockito.verify(sharedPrefs).writeSortPos(0)
        Mockito.verify(sharedPrefs).writeSortPos(1)
        Mockito.verify(sharedPrefs).writeSortPos(2)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldBeEmptyIfNoMovies() {
        Mockito.`when`(movieStorage.getOnlMovies(1, initialSort.option, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(emptyList())))
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(sort = initialSort, loading = false, movies = emptyList(), empty = true)
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectErrorMessageOnMovieLoadFail() {
        Mockito.`when`(movieStorage.getOnlMovies(1, initialSort.option, false))
                .thenReturn(Observable.just(GetMoviesResult.Failure))
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(
                        sort = initialSort,
                        loading = false,
                        empty = true,
                        snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed)
                ),
                GridState(
                        sort = initialSort,
                        loading = false,
                        empty = true,
                        snackbar = SnackbarMessage(false, R.string.snackbar_movies_load_failed)
                )
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.SnackbarShown)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldLoadNextPageOnScroll() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val page1Movies = getMovies(2)
        Mockito.`when`(movieStorage.getOnlMovies(1, SortOption.SORT_POPULARITY, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(page1Movies)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        val page2Movies = getMovies(5)
        Mockito.`when`(movieStorage.getOnlMovies(2, SortOption.SORT_POPULARITY, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(page2Movies)))
        val page2GridViewData = mapGridRowMovieViewData(page2Movies)

        val page1And2GridViewData = page1GridViewData.plus(page2GridViewData)

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(sort = initialSort, movies = page1GridViewData, loading = false),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData.plus(GridRowLoadMoreViewData()),
                        loadingMore = true),
                GridState(sort = initialSort, movies = page1And2GridViewData, loadingMore = false)
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.LoadMore(2))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectErrorMessageOnNextPageLoadFail() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val page1Movies = getMovies(2)
        Mockito.`when`(movieStorage.getOnlMovies(1, initialSort.option, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(page1Movies)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        Mockito.`when`(movieStorage.getOnlMovies(2, initialSort.option, false))
                .thenReturn(Observable.just(GetMoviesResult.Failure))

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(sort = initialSort, movies = page1GridViewData, loading = false),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData.plus(GridRowLoadMoreViewData()),
                        loadingMore = true
                ),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData,
                        loadingMore = false,
                        snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed)),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData,
                        snackbar = SnackbarMessage(false, R.string.snackbar_movies_load_failed)
                )
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.LoadMore(2))
        actions.accept(GridAction.SnackbarShown)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldRefreshAllPagesOnSwipe() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val page1Movies = getMovies(2)
        Mockito.`when`(movieStorage.getOnlMovies(1, SortOption.SORT_POPULARITY, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(page1Movies)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        val page2Movies = getMovies(5)
        Mockito.`when`(movieStorage.getOnlMovies(2, SortOption.SORT_POPULARITY, false))
                .thenReturn(Observable.just(GetMoviesResult.Success(page2Movies)))
        val page2GridViewData = mapGridRowMovieViewData(page2Movies)

        val page1And2GridViewData = page1GridViewData.plus(page2GridViewData)

        Mockito.`when`(movieStorage.getOnlMovies(2, SortOption.SORT_POPULARITY, true))
                .thenReturn(Observable.just(GetMoviesResult.Success(page1Movies.plus(page2Movies))))

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(sort = initialSort, movies = page1GridViewData, loading = false),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData.plus(GridRowLoadMoreViewData()),
                        loadingMore = true
                ),
                GridState(sort = initialSort, movies = page1And2GridViewData, loadingMore = false),
                GridState(sort = initialSort, movies = page1And2GridViewData, refreshing = true),
                GridState(sort = initialSort, movies = page1And2GridViewData, refreshing = false)
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.LoadMore(2))
        actions.accept(GridAction.RefreshSwipe)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectMessageOnDeleteFromFavClick() {
        Mockito.`when`(movieStorage.deleteMovieFromFav(0))
                .thenReturn(Observable.just(LocalDbWriteResult.DeleteFromFav(true)))
        Mockito.`when`(movieStorage.deleteMovieFromFav(1))
                .thenReturn(Observable.just(LocalDbWriteResult.DeleteFromFav(false)))

        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(
                        sort = initialSort,
                        snackbar = SnackbarMessage(true, R.string.snackbar_movie_removed_from_favorites)
                ),
                GridState(
                        sort = initialSort,
                        snackbar = SnackbarMessage(false, R.string.snackbar_movie_removed_from_favorites)
                ),
                GridState(sort = initialSort, snackbar = SnackbarMessage(true, R.string.snackbar_movie_delete_failed)),
                GridState(sort = initialSort, snackbar = SnackbarMessage(false, R.string.snackbar_movie_delete_failed))
        )

        actions.accept(GridAction.FavDelete(0))
        actions.accept(GridAction.SnackbarShown)
        actions.accept(GridAction.FavDelete(1))
        actions.accept(GridAction.SnackbarShown)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    private fun getMovies(count: Int): List<Movie> = (1..count)
            .map { Movie(it, "backdrop", "overview", Date(), "poster", "title", 12.5) }

    private fun mapGridRowMovieViewData(movies: List<Movie>): List<GridRowMovieViewData> = movies
            .map {
                GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                        it.poster, it.backdrop)
            }
}