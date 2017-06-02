package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.features.details.component.RS_REMOVE_FROM_FAV
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortSelectionState
import io.reactivex.Observable

fun intention(
        sources: GridSources,
        sortOptions: List<Sort>
): Observable<GridAction> {
    val snackbarShown = sources.uiEvents.snackbarShown
            .map { GridAction.SnackbarShown }

    val sortSelectionsSharedPrefs = sources.sharedPrefs
            .sortPos()
            .map { SortSelectionState(sortOptions[it], sortOptions[0]) }
    val sortSelectionsSpinner = sources.uiEvents.sortSelections
            .skip(1) // skip initial position 0 emission (spinner always emit this)
            .map { sortOptions[it] }
            .scan(SortSelectionState(sortOptions[0], sortOptions[0]),
                    { (sort), curr -> SortSelectionState(curr, sort) })
            .skip(1) // skip initial scan emission
//            .distinctUntilChanged()

    val sortSelections = Observable.merge(sortSelectionsSharedPrefs, sortSelectionsSpinner)
            .map { GridAction.SortSelection(it.sort, it.sortPrev) }
    val movieClicks = sources.uiEvents.movieClicks
            .map { GridAction.MovieClick(it) }
    val loadMore = sources.uiEvents.loadMore
            .map { 1 }
            .scan(1) { acc, curr -> acc + curr }
            .map { GridAction.LoadMore(it) }
    val refreshSwipes = sources.uiEvents.refreshSwipes
            .map { GridAction.RefreshSwipe }

    val favDelete = sources.activityResults
            .filter { it.requestCode == RQ_DETAILS && it.resultCode == RS_REMOVE_FROM_FAV }
            .map { GridAction.FavDelete }

    val actions = listOf(snackbarShown, sortSelections, movieClicks, loadMore, refreshSwipes, favDelete)
    return Observable.merge(actions)
}