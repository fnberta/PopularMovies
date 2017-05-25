package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.effects.LoaderTarget
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.view.LOADER_ONL_MOVIES
import ch.berta.fabio.popularmovies.features.grid.view.LoadOnlMoviesArgs
import rx.Observable

fun loadData(
        actions: Observable<GridAction>,
        state: Observable<GridState>
): Observable<LoaderTarget> {
    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)
            .filter {
                it.sort.option != SortOption.SORT_FAVORITE
                        && it.sortPrev.option != SortOption.SORT_FAVORITE
            }
            .map {
                val args = LoadOnlMoviesArgs(it.sort.value, 1)
                LoaderTarget(LOADER_ONL_MOVIES, args)
            }

    val refresh = actions
            .ofType(GridAction.RefreshSwipe::class.java)
    val loadMore = actions
            .ofType(GridAction.LoadMore::class.java)
    val refreshAndMore = Observable.merge(refresh, loadMore)
            .withLatestFrom(state, { _, state -> state })
            .map {
                val args = LoadOnlMoviesArgs(it.sort.value, it.page)
                LoaderTarget(LOADER_ONL_MOVIES, args)
            }

    val dataLoaders = listOf(sortSelections, refreshAndMore)
    return Observable.merge(dataLoaders)
}
