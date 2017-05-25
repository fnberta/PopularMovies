package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.features.details.component.RS_FAV_DELETED
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortSelectionState
import rx.Observable

fun intention(
        sources: GridSources,
        initialSort: Sort,
        sortOptions: List<Sort>
): Observable<GridAction> {
    val initSortSelection = SortSelectionState(initialSort, initialSort)
    val sortSelections = sources.viewEvents.sortSelections
            .skip(2) // skip initial double position 0 emission (spinner bug)
            .map { sortOptions[it] }
            .scan(initSortSelection, { (sort), curr -> SortSelectionState(curr, sort) })
            .skip(1) // skip initial scan emission
            .map { GridAction.SortSelection(it.sort, it.sortPrev) }
    val movieClicks = sources.viewEvents.movieClicks
            .map { GridAction.MovieClick(it) }
    val loadMore = sources.viewEvents.loadMore
            .map { GridAction.LoadMore }
    val refreshSwipes = sources.viewEvents.refreshSwipes
            .map { GridAction.RefreshSwipe }

    val moviesOnlLoad = sources.dataLoadEvents.moviesOnl
            .map { GridAction.MoviesOnlLoad(it) }
    val moviesFavLoad = sources.dataLoadEvents.moviesFav
            .map { GridAction.MoviesFavLoad(it) }

    val favDelete = sources.frameworkEvents.activityResult
            .filter { it.requestCode == RQ_DETAILS && it.resultCode == RS_FAV_DELETED }
            .map { GridAction.FavDelete }

    val actions = listOf(sortSelections, movieClicks, loadMore, refreshSwipes, moviesOnlLoad,
            moviesFavLoad, favDelete)
    return Observable.merge(actions)
}