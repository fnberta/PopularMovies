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

import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.GetMoviesResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowLoadMoreViewData
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowMovieViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.formatLong
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom

typealias GridStateReducer = (GridState) -> GridState

data class PageWithSort(val page: Int, val sort: Sort)

fun model(
        sortOptions: List<Sort>,
        initialState: GridState,
        actions: Observable<GridAction>,
        movieStorage: MovieStorage,
        sharedPrefs: SharedPrefs
): Observable<GridState> {
    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)
            .map { it.sort }
            .share()

    val pageWithSortSelections = actions
            .ofType(GridAction.LoadMore::class.java)
            .withLatestFrom(sortSelections, { (page), sort -> PageWithSort(page, sort) })
            .share()

    val clearTransient = actions
            .ofType(GridAction.TransientClear::class.java)
            .map { clearTransientReducer() }

    val movies = sortSelections
            .flatMap { selection ->
                sharedPrefs.writeSortPos(sortOptions.indexOf(selection)).map { selection }
            }
            .switchMap {
                if (it.option == SortOption.SORT_FAVORITE) {
                    movieStorage.getFavMovies().map { moviesReducer(it, true, false) }
                } else {
                    movieStorage.getOnlMovies(1, it.option, false).map { moviesReducer(it, false, false) }
                }.startWith(sortSelectionReducer(it))
            }

    val loadMoreMovies = pageWithSortSelections.switchMap {
        movieStorage.getOnlMovies(it.page, it.sort.option, false)
                .map(::moviesOnlMoreReducer)
                .startWith(loadMoreMoviesReducer(GridRowLoadMoreViewData()))
    }

    val refreshMovies = actions
            .ofType(GridAction.RefreshSwipe::class.java)
            .withLatestFrom(pageWithSortSelections, { _, pws -> pws })
            .switchMap {
                movieStorage.getOnlMovies(it.page, it.sort.option, true)
                        .map { moviesReducer(it, false, true) }
                        .startWith(refreshMoviesReducer())
            }

    val deleteFavMovieResults = actions
            .ofType(GridAction.MovieFavDeleted::class.java)
            .map { deleteFavMovieResultReducer() }

    val movieSelections = actions
            .ofType(GridAction.MovieSelection::class.java)
            .map { it.selectedMovie }
            .map(::movieSelectionReducer)

    val reducers = listOf(clearTransient, movies, loadMoreMovies, refreshMovies, deleteFavMovieResults, movieSelections)
    return Observable.merge(reducers)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
            .distinctUntilChanged()
}

private fun clearTransientReducer(): GridStateReducer = {
    it.copy(message = null, selectedMovie = null)
}

private fun sortSelectionReducer(sort: Sort): GridStateReducer = {
    it.copy(sort = sort, loading = true)
}

private fun refreshMoviesReducer(): GridStateReducer = { it.copy(refreshing = true) }

private fun loadMoreMoviesReducer(loadMoreViewData: GridRowLoadMoreViewData): GridStateReducer = {
    it.copy(loadingMore = true, movies = it.movies.plus(loadMoreViewData))
}

private fun moviesReducer(result: GetMoviesResult, fromFav: Boolean, diffTrans: Boolean): GridStateReducer = { state ->
    when (result) {
        is GetMoviesResult.Failure -> state.copy(
                loading = false,
                refreshing = false,
                movies = emptyList(),
                empty = true,
                message = R.string.snackbar_movies_load_failed
        )
        is GetMoviesResult.Success -> {
            val movies = result.movies.map {
                GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                        it.poster, it.backdrop, fromFav)
            }
            state.copy(
                    movies = movies,
                    diffTransition = diffTrans,
                    empty = movies.isEmpty(),
                    loading = false,
                    loadingMore = false,
                    refreshing = false
            )
        }
    }
}

private fun moviesOnlMoreReducer(result: GetMoviesResult): GridStateReducer = { state ->
    when (result) {
        is GetMoviesResult.Failure -> state.copy(
                loadingMore = false,
                movies = state.movies.minus(state.movies.last()),
                diffTransition = true,
                message = R.string.snackbar_movies_load_failed
        )
        is GetMoviesResult.Success -> {
            val movies = result.movies.map {
                GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                        it.poster, it.backdrop, false)
            }
            state.copy(
                    movies = state.movies.minus(state.movies.last()).plus(movies),
                    diffTransition = true,
                    loadingMore = false
            )
        }
    }
}

private fun deleteFavMovieResultReducer(): GridStateReducer = {
    it.copy(message = R.string.snackbar_movie_removed_from_favorites)
}

private fun movieSelectionReducer(selectedMovie: SelectedMovie): GridStateReducer = {
    it.copy(selectedMovie = selectedMovie)
}
