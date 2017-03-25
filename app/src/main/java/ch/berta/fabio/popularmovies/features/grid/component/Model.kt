package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.extensions.debug
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.details.DetailsActivity
import ch.berta.fabio.popularmovies.features.grid.GridFavFragment
import ch.berta.fabio.popularmovies.features.grid.GridOnlFragment
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlRowViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridRowViewModel
import ch.berta.fabio.popularmovies.utils.formatDateLong
import rx.Observable

typealias MovieGridReducer = (GridViewState) -> GridViewState

data class MovieGridIntentions(
        val activityResult: Observable<ActivityResult>,
        val activityStarted: Observable<String>,
        val fragmentCommitted: Observable<String>,
        val moviesOnl: Observable<List<Movie>>,
        val moviesFav: Observable<Sequence<Map<String, Any?>>>,
        val sortSelections: Observable<Int>,
        val movieClicks: Observable<Int>,
        val loadMore: Observable<Unit>,
        val refreshSwipes: Observable<Unit>
)

fun model(
        initialState: GridViewState,
        intentions: MovieGridIntentions,
        moviePosterHeight: Int
): Observable<GridViewState> {
    val fragmentCommitted = intentions.fragmentCommitted
            .map(::fragmentCommittedReducer)
    val moviesOnl = intentions.moviesOnl
            .flatMap {
                Observable.from(it)
                        .map {
                            GridRowViewModel(R.layout.row_movie, it.title,
                                    formatDateLong(it.releaseDate), it.posterPath,
                                    moviePosterHeight)
                        }
                        .toList()
            }
            .debug("moviesOnl")
            .map(::moviesOnlReducer)
    val refreshSwipes = intentions.refreshSwipes
            .map { true }
            .map(::refreshSwipesReducer)
    val moviesFav = intentions.moviesFav
            .debug("moviesFav")
            .map(::moviesFavReducer)
    val sortSelections = intentions.sortSelections
            .filter { it != -1 }
            .distinctUntilChanged()
            .debug("sortSelection")
            .map(::sortSelectionsReducer)
    val movieClicks = Observable.merge(
            intentions.movieClicks,
            intentions.activityStarted
                    .filter { it == DetailsActivity::class.java.canonicalName }
                    .map { -1 }
    )
            .debug("movieClick")
            .map(::movieClicksReducer)

    val reducer = listOf(fragmentCommitted, moviesOnl, refreshSwipes, moviesFav, sortSelections,
            movieClicks)
    return Observable.merge(reducer)
            .scan(initialState, { state, reducer -> reducer(state) })
            .debug("state")
            .publish()
            .autoConnect(2)
}

fun moviesOnlReducer(movies: List<GridOnlRowViewModel>): MovieGridReducer = { state ->
    state.copy(
            moviesOnl = movies,
            loading = false,
            refreshing = false,
            refreshMoviesOnl = false,
            loadNewSort = false
    )
}

fun refreshSwipesReducer(refreshMoviesOnl: Boolean): MovieGridReducer = { state ->
    state.copy(refreshMoviesOnl = refreshMoviesOnl)
}

fun moviesFavReducer(movies: Sequence<Map<String, Any?>>): MovieGridReducer = { state ->
    state.copy(moviesFav = movies, loading = false)
}

fun fragmentCommittedReducer(tag: String): MovieGridReducer = { state ->
    when (tag) {
        GridOnlFragment::class.java.canonicalName -> state.copy(showOnlGrid = false)
        GridFavFragment::class.java.canonicalName -> state.copy(showFavGrid = false)
        else -> state
    }
}

fun sortSelectionsReducer(sortSelectedPos: Int): MovieGridReducer = { state ->
    val sortSelectedOption = state.sortOptions[sortSelectedPos].option
    val currentSortSelectedOption = state.sortOptions[state.sortSelectedPos].option
    val showFavGrid = sortSelectedOption == SortOption.SORT_FAVORITE
    val showOnlGrid = sortSelectedOption != SortOption.SORT_FAVORITE &&
            currentSortSelectedOption == SortOption.SORT_FAVORITE
    val loadNewSort = sortSelectedOption != SortOption.SORT_FAVORITE &&
            sortSelectedOption != currentSortSelectedOption
    state.copy(
            sortSelectedPos = sortSelectedPos,
            showFavGrid = showFavGrid,
            showOnlGrid = showOnlGrid,
            loadNewSort = loadNewSort
    )
}

fun movieClicksReducer(pos: Int): MovieGridReducer = { state ->
    state.copy(selectedMoviePosition = pos, showMovieDetails = pos != -1)
}
