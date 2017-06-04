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
import ch.berta.fabio.popularmovies.data.GetFavMoviesResult
import ch.berta.fabio.popularmovies.data.GetOnlMoviesResult
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
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
        val loading: Boolean = true,
        val loadingMore: Boolean = false,
        val refreshing: Boolean = false,
        val snackbar: SnackbarMessage = SnackbarMessage(false)
)

typealias GridStateReducer = (GridState) -> GridState

data class PageWithSort(val page: Int, val sort: Sort)

fun model(
        sortOptions: List<Sort>,
        actions: Observable<GridAction>,
        movieStorage: MovieStorage,
        localDbWriteResults: Observable<LocalDbWriteResult>
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
                            .map(::moviesFavReducer)
                } else {
                    movieStorage.getOnlMovies(1, it.sort.value, false)
                            .map(::moviesOnlReducer)
                }.startWith(sortSelectionsReducer(it))
            }

    val pageWithSort = actions
            .ofType(GridAction.LoadMore::class.java)
            .withLatestFrom(sortSelectionActions,
                    BiFunction<GridAction.LoadMore, GridAction.SortSelection, PageWithSort>
                    { (page), (sort) -> PageWithSort(page, sort) })

    val loadMore = pageWithSort
            .skip(1) // skip initial page 1 emission
            .switchMap {
                movieStorage.getOnlMovies(it.page, it.sort.value, false)
                        .map(::moviesOnlMoreReducer)
                        .startWith(loadMoreReducer(GridRowLoadMoreViewData()))
            }

    val refreshSwipes = actions
            .ofType(GridAction.RefreshSwipe::class.java)
            .withLatestFrom(pageWithSort, BiFunction<GridAction, PageWithSort, PageWithSort>
            { _, pageWithSort -> pageWithSort })
            .switchMap {
                movieStorage.getOnlMovies(it.page, it.sort.value, true)
                        .map(::moviesOnlReducer)
                        .startWith(refreshingReducer())
            }


    val favDelete = localDbWriteResults
            .ofType(LocalDbWriteResult.DeleteFromFav::class.java)
            .map { it.successful }
            .map(::favDeleteReducer)

    val initialState = GridState(sortOptions[0])
    val reducer = listOf(snackbars, sortSelections, loadMore, refreshSwipes, favDelete)
    return Observable.merge(reducer)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
}

fun snackbarReducer(): GridStateReducer = {
    it.copy(snackbar = it.snackbar.copy(show = false))
}

fun sortSelectionsReducer(sortSelection: GridAction.SortSelection): GridStateReducer = {
    it.copy(sort = sortSelection.sort, loading = true)
}

fun refreshingReducer(): GridStateReducer = {
    it.copy(refreshing = true)
}

fun loadMoreReducer(loadMoreViewData: GridRowLoadMoreViewData): GridStateReducer = {
    it.copy(loadingMore = true, movies = it.movies.plus(loadMoreViewData))
}

fun moviesOnlReducer(result: GetOnlMoviesResult): GridStateReducer = { state ->
    when (result) {
        is GetOnlMoviesResult.Failure -> state.copy(loading = false, refreshing = false, empty = true,
                snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed))
        is GetOnlMoviesResult.Success -> {
            val movies = result.movies
                    .map { GridRowMovieViewData(it.id, it.title, it.releaseDate.formatLong(), it.poster) }
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

fun moviesFavReducer(result: GetFavMoviesResult): GridStateReducer = { state ->
    when (result) {
        is GetFavMoviesResult.Failure -> state.copy(loading = false, refreshing = false, empty = true,
                snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed))
        is GetFavMoviesResult.Success -> {
            val movies = result.movies
                    .map { GridRowMovieViewData(it.id, it.title, it.releaseDate.formatLong(), it.poster) }
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

fun moviesOnlMoreReducer(result: GetOnlMoviesResult): GridStateReducer = { state ->
    when (result) {
        is GetOnlMoviesResult.Failure -> state.copy(loadingMore = false,
                snackbar = SnackbarMessage(true, R.string.snackbar_movies_load_failed))
        is GetOnlMoviesResult.Success -> {
            val movies = result.movies
                    .map { GridRowMovieViewData(it.id, it.title, it.releaseDate.formatLong(), it.poster) }
            state.copy(movies = state.movies.minus(state.movies.last()).plus(movies), loadingMore = false)
        }
    }
}

fun favDeleteReducer(successful: Boolean): GridStateReducer = {
    it.copy(snackbar = SnackbarMessage(true,
            if (successful) R.string.snackbar_movie_removed_from_favorites
            else R.string.snackbar_movie_delete_failed))
}
