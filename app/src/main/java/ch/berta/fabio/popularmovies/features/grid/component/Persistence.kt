package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.effects.KEY_SORT_POS
import ch.berta.fabio.popularmovies.effects.SharedPrefsWriteTarget
import ch.berta.fabio.popularmovies.features.grid.Sort
import rx.Observable


fun persist(
        actions: Observable<GridAction>,
        sortOptions: List<Sort>
): Observable<SharedPrefsWriteTarget> = actions
        .ofType(GridAction.SortSelection::class.java)
        .map { sortOptions.indexOf(it.sort) }
        .map { SharedPrefsWriteTarget(KEY_SORT_POS, it) }
