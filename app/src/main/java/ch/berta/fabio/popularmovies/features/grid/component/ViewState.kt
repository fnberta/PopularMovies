package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.SortOption.*
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlRowViewModel

data class GridViewState(
        val sortOptions: List<Sort>,
        val sortSelectedPos: Int,
        val moviesOnl: List<GridOnlRowViewModel> = emptyList(),
        val moviesFav: Sequence<Map<String, Any?>> = emptySequence(),
        val selectedMoviePosition: Int,
        val loading: Boolean,
        val refreshing: Boolean,
        val refreshMoviesOnl: Boolean,
        val showOnlGrid: Boolean,
        val showFavGrid: Boolean,
        val loadNewSort: Boolean,
        val showMovieDetails: Boolean,
        val hideMovieDetails: Boolean
)

fun createInitialState(
        initSortOptionsPos: Int,
        getSortOptionDisplay: (SortOption) -> Sort
): GridViewState {
    val sortOptions = listOf(SORT_POPULARITY, SORT_RATING, SORT_RELEASE_DATE, SORT_FAVORITE)
    val sortOptionsDisplay = sortOptions.map { getSortOptionDisplay(it) }

    return GridViewState(sortOptionsDisplay, initSortOptionsPos, emptyList(), emptySequence(),
            -1, true, false, false, false, false, false, false, false)
}