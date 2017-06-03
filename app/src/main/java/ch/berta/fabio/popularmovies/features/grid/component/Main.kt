package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

data class GridSources(
        val uiEvents: GridUiEvents,
        val activityResults: Observable<ActivityResult>,
        val sharedPrefs: SharedPrefs,
        val movieStorage: MovieStorage,
        val localDbWriteResults: Observable<LocalDbWriteResult>
)

data class GridUiEvents(
        val snackbarShown: PublishRelay<Unit> = PublishRelay.create(),
        val sortSelections: PublishRelay<Int> = PublishRelay.create(),
        val movieClicks: PublishRelay<SelectedMovie> = PublishRelay.create(),
        val loadMore: PublishRelay<Unit> = PublishRelay.create(),
        val refreshSwipes: PublishRelay<Unit> = PublishRelay.create()
)

sealed class GridAction {
    object SnackbarShown : GridAction()
    data class SortSelection(val sort: Sort, val sortPrev: Sort) : GridAction()
    data class MovieClick(val selectedMovie: SelectedMovie) : GridAction()
    data class LoadMore(val page: Int) : GridAction()
    object RefreshSwipe : GridAction()
    data class FavDelete(val movieId: Int) : GridAction()
}

sealed class GridSink {
    data class State(val state: GridState) : GridSink()
    data class Navigation(val target: NavigationTarget) : GridSink()
    object SharedPrefsWrite : GridSink()
    data class LocalDbWrite(val result: LocalDbWriteResult) : GridSink()
}

fun main(
        sources: GridSources,
        sortOptions: List<Sort>
): Observable<GridSink> = intention(sources, sortOptions)
        .log("action")
        .publish {
            val state = model(sortOptions, it, sources.movieStorage, sources.localDbWriteResults)
                    .map { GridSink.State(it) }
            val navigationTargets = navigationTargets(it)
                    .map { GridSink.Navigation(it) }
            val sharedPrefWrites = sharedPrefWrites(it, sources.sharedPrefs, sortOptions)
                    .map { GridSink.SharedPrefsWrite }
            val localDbWrites = localMovieDbWrites(it, sources.movieStorage)
                    .map { GridSink.LocalDbWrite(it) }

            Observable.merge(state, navigationTargets, sharedPrefWrites, localDbWrites)
        }
        .share()
