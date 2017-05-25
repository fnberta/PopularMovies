package ch.berta.fabio.popularmovies.features.grid.component

import android.database.Cursor
import android.view.View
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.effects.LoaderTarget
import ch.berta.fabio.popularmovies.effects.NavigationTarget
import ch.berta.fabio.popularmovies.effects.SharedPrefsWriteTarget
import ch.berta.fabio.popularmovies.extensions.debug
import ch.berta.fabio.popularmovies.features.base.FrameworkEvents
import ch.berta.fabio.popularmovies.features.grid.Sort
import rx.Observable

data class GridSources(
        val viewEvents: GridViewEvents,
        val frameworkEvents: FrameworkEvents,
        val dataLoadEvents: GridDataLoadEvents
)

data class GridViewEvents(
        val sortSelections: Observable<Int>,
        val movieClicks: Observable<SelectedMovie>,
        val loadMore: Observable<Unit>,
        val refreshSwipes: Observable<Unit>
)

sealed class SelectedMovie {
    data class Onl(val id: Int, val posterView: View?) : SelectedMovie()
    data class Fav(val id: Long, val posterView: View?) : SelectedMovie()
}

data class GridDataLoadEvents(
        val moviesOnl: Observable<List<Movie>>,
        val moviesFav: Observable<Maybe<Cursor>>
)

sealed class GridAction {
    data class MoviesOnlLoad(val movies: List<Movie>) : GridAction()
    data class MoviesFavLoad(val favResult: Maybe<Cursor>) : GridAction()
    data class SortSelection(val sort: Sort, val sortPrev: Sort) : GridAction()
    data class MovieClick(val selectedMovie: SelectedMovie) : GridAction()
    object LoadMore : GridAction()
    object RefreshSwipe : GridAction()
    object FavDelete : GridAction()
}

data class GridSinks(
        val state: Observable<GridState>,
        val navigation: Observable<NavigationTarget>,
        val loader: Observable<LoaderTarget>,
        val sharedPrefs: Observable<SharedPrefsWriteTarget>
)

fun main(
        initialState: GridState,
        sources: GridSources,
        sortOptions: List<Sort>,
        moviePosterHeight: Int
): GridSinks {
    val actions = intention(sources, initialState.sort, sortOptions)
            .debug("action")
            .share()
    val navigation = navigate(actions)
            .share()
    val state = model(actions, initialState, moviePosterHeight)
            .share()
    val loader = loadData(actions, state)
            .share()
    val sharedPrefs = persist(actions, sortOptions)
            .share()

    return GridSinks(state, navigation, loader, sharedPrefs)
}
