package ch.berta.fabio.popularmovies.features.grid.component

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.features.common.SnackbarMessage
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridOnlRowLoadMoreViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridOnlRowViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridRowViewModel
import ch.berta.fabio.popularmovies.utils.formatDateLong
import rx.Observable

data class GridState(
        val sort: Sort,
        val page: Int,
        val moviesOnl: List<GridOnlRowViewModel> = emptyList(),
        val moviesFav: Maybe<Cursor> = Maybe.None,
        val loading: Boolean,
        val loadingMore: Boolean,
        val refreshing: Boolean,
        val snackbar: SnackbarMessage
)

typealias GridStateReducer = (GridState) -> GridState

fun model(
        actions: Observable<GridAction>,
        initialState: GridState,
        moviePosterHeight: Int
): Observable<GridState> {
    val moviesOnl = actions
            .ofType(GridAction.MoviesOnlLoad::class.java)
            .switchMap {
                Observable.from(it.movies)
                        .map {
                            GridRowViewModel(it.dbId, it.title, formatDateLong(it.releaseDate),
                                    it.posterPath, moviePosterHeight)
                        }
                        .toList()
            }
            .map(::moviesOnlReducer)
    val moviesFav = actions
            .ofType(GridAction.MoviesFavLoad::class.java)
            .map { it.favResult }
            .map(::moviesFavReducer)

    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)
            .map(::sortSelectionsReducer)
    val refreshSwipes = actions
            .ofType(GridAction.RefreshSwipe::class.java)
            .map { refreshingReducer() }
    val loadMore = actions
            .ofType(GridAction.LoadMore::class.java)
            .map { GridOnlRowLoadMoreViewModel() }
            .map(::loadMoreReducer)

    val favDelete = actions
            .ofType(GridAction.FavDelete::class.java)
            .map { favDeleteReducer() }

    val reducer = listOf(moviesOnl, moviesFav, sortSelections, refreshSwipes, loadMore, favDelete)
    return Observable.merge(reducer)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
}

fun createInitialState(initialSort: Sort): GridState =
        GridState(initialSort, 1, emptyList(), Maybe.None, true, false, false,
                SnackbarMessage(false))

fun sortSelectionsReducer(sortSelection: GridAction.SortSelection): GridStateReducer = {
    it.copy(sort = sortSelection.sort, snackbar = it.snackbar.copy(show = false))
}

fun refreshingReducer(): GridStateReducer = {
    it.copy(refreshing = true, snackbar = it.snackbar.copy(show = false))
}

fun loadMoreReducer(loadMoreViewModel: GridOnlRowLoadMoreViewModel): GridStateReducer = {
    it.copy(
            loadingMore = true,
            page = it.page + 1,
            moviesOnl = it.moviesOnl.plus(loadMoreViewModel),
            snackbar = it.snackbar.copy(show = false)
    )
}

fun moviesOnlReducer(movies: List<GridOnlRowViewModel>): GridStateReducer = {
    it.copy(
            moviesOnl = movies,
            loading = false,
            loadingMore = false,
            refreshing = false,
            snackbar = it.snackbar.copy(show = false)
    )
}

fun moviesFavReducer(favResult: Maybe<Cursor>): GridStateReducer = {
    it.copy(moviesFav = favResult, loading = false, snackbar = it.snackbar.copy(show = false))
}

fun favDeleteReducer(): GridStateReducer = {
    val snackbar = it.snackbar.copy(
            show = true,
            message = R.string.snackbar_movie_removed_from_favorites
    )
    it.copy(snackbar = snackbar)
}
