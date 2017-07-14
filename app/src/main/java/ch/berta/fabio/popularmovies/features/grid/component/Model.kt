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
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.common.SnackbarMessage
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowLoadMoreViewData
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowMovieViewData
import ch.berta.fabio.popularmovies.features.grid.vdos.rows.GridRowViewData
import ch.berta.fabio.popularmovies.formatLong
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

data class GridState(
        val sort: Sort,
        val movies: List<GridRowViewData> = emptyList(),
        val empty: Boolean = false,
        val loading: Boolean = false,
        val loadingMore: Boolean = false,
        val refreshing: Boolean = false,
        val snackbar: SnackbarMessage = SnackbarMessage(false)
)

typealias GridStateReducer = (GridState) -> GridState

data class PageWithSort(val page: Int, val sort: Sort)

fun model(
        sortOptions: List<Sort>,
        initialState: GridState,
        actions: Observable<GridAction>,
        movieStorage: MovieStorage,
        sharedPrefs: SharedPrefs
): Observable<GridState> {
    val snackbars = actions
            .ofType(GridAction.SnackbarShown::class.java)
            .map { snackbarReducer() }

    val sortSelectionActions = actions
            .ofType(GridAction.SortSelection::class.java)

    val sortSelections = sortSelectionActions
            .switchMap {
                if (it.sort.option == SortOption.SORT_FAVORITE) {
                    movieStorage.getFavMovies()
                            .map(::moviesReducer)
                } else {
                    movieStorage.getOnlMovies(1, it.sort.option, false)
                            .map(::moviesReducer)
                }.startWith(sortSelectionsReducer(it))
            }

    val sortSelectionSave = sortSelectionActions
            .map { sortOptions.indexOf(it.sort) }
            .flatMap { sharedPrefs.writeSortPos(it) }
            .map { sortSelectionSaveReducer() }

    val pageWithSort = actions
            .ofType(GridAction.LoadMore::class.java)
            .withLatestFrom(sortSelectionActions,
                    BiFunction<GridAction.LoadMore, GridAction.SortSelection, PageWithSort>
                    { (page), (sort) -> PageWithSort(page, sort) })

    val loadMore = pageWithSort
            .switchMap {
                movieStorage.getOnlMovies(it.page, it.sort.option, false)
                        .map(::moviesOnlMoreReducer)
                        .startWith(loadMoreReducer(GridRowLoadMoreViewData()))
            }

    val refreshSwipes = actions
            .ofType(GridAction.RefreshSwipe::class.java)
            .withLatestFrom(pageWithSort, BiFunction<GridAction, PageWithSort, PageWithSort>
            { _, pageWithSort -> pageWithSort })
            .switchMap {
                movieStorage.getOnlMovies(it.page, it.sort.option, true)
                        .map(::moviesReducer)
                        .startWith(refreshingReducer())
            }

    val favDelete = actions
            .ofType(GridAction.FavDelete::class.java)
            .flatMap { movieStorage.deleteMovieFromFav(it.movieId) }
            .map(::favDeleteReducer)

    val reducers = listOf(snackbars, sortSelections, sortSelectionSave, loadMore, refreshSwipes, favDelete)
    return Observable.merge(reducers)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
            .distinctUntilChanged()
}

private fun snackbarReducer(): GridStateReducer = {
    it.copy(snackbar = it.snackbar.copy(show = false))
}

private fun sortSelectionsReducer(sortSelection: GridAction.SortSelection): GridStateReducer = {
    it.copy(sort = sortSelection.sort, loading = true)
}

private fun sortSelectionSaveReducer(): GridStateReducer = { it }

private fun refreshingReducer(): GridStateReducer = { it.copy(refreshing = true) }

private fun loadMoreReducer(loadMoreViewData: GridRowLoadMoreViewData): GridStateReducer = {
    it.copy(loadingMore = true, movies = it.movies.plus(loadMoreViewData))
}

private fun moviesReducer(result: GetMoviesResult): GridStateReducer = { state ->
    when (result) {
        is GetMoviesResult.Failure -> state.copy(
                loading = false,
                refreshing = false,
                movies = emptyList(),
                empty = true,
                snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed))
        is GetMoviesResult.Success -> {
            val movies = result.movies
                    .map {
                        GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                                it.poster, it.backdrop)
                    }
            state.copy(
                    movies = movies,
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
                snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed))
        is GetMoviesResult.Success -> {
            val movies = result.movies
                    .map {
                        GridRowMovieViewData(it.id, it.title, it.overview, it.releaseDate.formatLong(), it.voteAverage,
                                it.poster, it.backdrop)
                    }
            state.copy(movies = state.movies.minus(state.movies.last()).plus(movies), loadingMore = false)
        }
    }
}

private fun favDeleteReducer(result: LocalDbWriteResult.DeleteFromFav): GridStateReducer = {
    it.copy(snackbar = SnackbarMessage(true,
            if (result.successful) R.string.snackbar_movie_removed_from_favorites
            else R.string.snackbar_movie_delete_failed))
}
