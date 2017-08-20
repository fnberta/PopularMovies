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
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.data.dtos.Movie
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.MovieDao
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.MovieEntity
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MoviesPage
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.makeSortOptions
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowLoadMoreViewData
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowMovieViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.formatLong
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class ModelTest {

    private val sharedPrefs: SharedPrefs = Mockito.mock(SharedPrefs::class.java)
    private val theMovieDbService: TheMovieDbService = Mockito.mock(TheMovieDbService::class.java)
    private val movieDao: MovieDao = Mockito.mock(MovieDao::class.java)
    private val movieDb: MovieDb = Mockito.mock(MovieDb::class.java)
    private val movieStorage = MovieStorage(theMovieDbService, movieDb)

    /*
    * 0: by popularity
    * 1: by rating
    * 2: by release date
    * 3: favorites
    */
    private val sortOptions = makeSortOptions { "someRandomTitle" }
    private val initialSort = sortOptions[0]
    private val actions: PublishRelay<GridAction> = PublishRelay.create()

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun setUp() {
        Mockito.`when`(sharedPrefs.writeSortPos(Mockito.anyInt())).thenReturn(Observable.just(Unit))
        Mockito.`when`(movieDb.movieDao()).thenReturn(movieDao)
    }

    @Test
    fun shouldLoadFavMoviesOnSelection() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        // expected grid state result emissions
        val favMovies = getMovieEntities(3)
        Mockito.`when`(movieDao.getAll()).thenReturn(Flowable.just(favMovies))
        val gridViewData = mapGridRowMovieEntityViewData(favMovies)
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
        Mockito.`when`(theMovieDbService.loadMovies(1, SortOption.SORT_POPULARITY.value))
                .thenReturn(Single.just(MoviesPage(1, popMovies, 1, popMovies.size)))
        val popGridViewData = mapGridRowMovieViewData(popMovies)

        val ratingMovies = getMovies(5)
        Mockito.`when`(theMovieDbService.loadMovies(1, SortOption.SORT_RATING.value))
                .thenReturn(Single.just(MoviesPage(1, ratingMovies, 1, ratingMovies.size)))
        val ratingGridViewData = mapGridRowMovieViewData(ratingMovies)

        val dateMovies = getMovies(7)
        Mockito.`when`(theMovieDbService.loadMovies(1, SortOption.SORT_RELEASE_DATE.value))
                .thenReturn(Single.just(MoviesPage(1, dateMovies, 1, dateMovies.size)))
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
        actions.accept(GridAction.TransientClear)

        Mockito.verify(sharedPrefs).writeSortPos(0)
        Mockito.verify(sharedPrefs).writeSortPos(1)
        Mockito.verify(sharedPrefs).writeSortPos(2)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldBeEmptyIfNoMovies() {
        Mockito.`when`(theMovieDbService.loadMovies(1, initialSort.option.value))
                .thenReturn(Single.just(MoviesPage(1, emptyList(), 1, 0)))
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
    fun shouldHaveCorrectErrorMessageOnFavMovieLoadFail() {
        Mockito.`when`(movieDao.getAll()).thenReturn(Flowable.error(Throwable("fav movies error")))
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(sort = sortOptions[3], loading = true),
                GridState(
                        sort = sortOptions[3],
                        loading = false,
                        empty = true,
                        message = R.string.snackbar_movies_load_failed
                ),
                GridState(
                        sort = sortOptions[3],
                        loading = false,
                        empty = true,
                        message = null
                )
        )

        actions.accept(GridAction.SortSelection(sortOptions[3], initialSort))
        actions.accept(GridAction.TransientClear)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectErrorMessageOnOnlMovieLoadFail() {
        Mockito.`when`(theMovieDbService.loadMovies(1, initialSort.option.value))
                .thenReturn(Single.error(Throwable("onl movies error")))
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(sort = initialSort, loading = true),
                GridState(
                        sort = initialSort,
                        loading = false,
                        empty = true,
                        message = R.string.snackbar_movies_load_failed
                ),
                GridState(
                        sort = initialSort,
                        loading = false,
                        empty = true,
                        message = null
                )
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.TransientClear)

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
        Mockito.`when`(theMovieDbService.loadMovies(1, SortOption.SORT_POPULARITY.value))
                .thenReturn(Single.just(MoviesPage(1, page1Movies, 1, page1Movies.size)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        val page2Movies = getMovies(5)
        Mockito.`when`(theMovieDbService.loadMovies(2, SortOption.SORT_POPULARITY.value))
                .thenReturn(Single.just(MoviesPage(2, page2Movies, 2, page1Movies.size + page2Movies.size)))
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
        Mockito.`when`(theMovieDbService.loadMovies(1, initialSort.option.value))
                .thenReturn(Single.just(MoviesPage(1, page1Movies, 1, page1Movies.size)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        Mockito.`when`(theMovieDbService.loadMovies(2, initialSort.option.value))
                .thenReturn(Single.error(Throwable("onl movies error")))

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
                        message = R.string.snackbar_movies_load_failed
                ),
                GridState(
                        sort = initialSort,
                        movies = page1GridViewData,
                        message = null
                )
        )

        actions.accept(GridAction.SortSelection(initialSort, initialSort))
        actions.accept(GridAction.LoadMore(2))
        actions.accept(GridAction.TransientClear)

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
        Mockito.`when`(theMovieDbService.loadMovies(1, SortOption.SORT_POPULARITY.value))
                .thenReturn(Single.just(MoviesPage(1, page1Movies, 1, page1Movies.size)))
        val page1GridViewData = mapGridRowMovieViewData(page1Movies)

        val page2Movies = getMovies(5)
        Mockito.`when`(theMovieDbService.loadMovies(2, SortOption.SORT_POPULARITY.value))
                .thenReturn(Single.just(MoviesPage(2, page2Movies, 2, page1Movies.size + page2Movies.size)))
        val page2GridViewData = mapGridRowMovieViewData(page2Movies)

        val page1And2GridViewData = page1GridViewData.plus(page2GridViewData)

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
    fun shouldHaveCorrectMessageOnDeletedFromFavResult() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val expectedStates = listOf(
                GridState(sort = initialSort, message = R.string.snackbar_movie_removed_from_favorites),
                GridState(sort = initialSort, message = null)
        )

        actions.accept(GridAction.MovieFavDeleted)
        actions.accept(GridAction.TransientClear)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectSelectedMovieOnClick() {
        val state = model(sortOptions, GridState(initialSort), actions, movieStorage, sharedPrefs)
        val observer = state.test()
        observer.assertNoValues()

        val selectedMovieOnl = SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", false)
        val selectedMovieFav = SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", true)

        val expectedStates = listOf(
                GridState(sort = initialSort, selectedMovie = selectedMovieOnl),
                GridState(sort = initialSort, selectedMovie = selectedMovieFav)
        )

        actions.accept(GridAction.MovieSelection(selectedMovieOnl))
        actions.accept(GridAction.MovieSelection(selectedMovieFav))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    private fun getMovies(count: Int): List<Movie> = (1..count)
            .map { Movie(it, "backdrop", "overview", Date(), "poster", "title", 12.5) }

    private fun mapGridRowMovieViewData(movies: List<Movie>): List<GridRowMovieViewData> = movies
            .map {
                GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                        it.poster, it.backdrop, false)
            }

    private fun getMovieEntities(count: Int): List<MovieEntity> = (1..count)
            .map { MovieEntity(it, "title", Date(), 12.5, "overview", "poster", "backdrop") }

    private fun mapGridRowMovieEntityViewData(movies: List<MovieEntity>): List<GridRowMovieViewData> = movies
            .map {
                GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                        it.poster, it.backdrop, true)
            }
}