package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.grid.Sort
import io.reactivex.Observable

const val KEY_SORT_POS = "KEY_SORT_POS"

fun sharedPrefWrites(
        actions: Observable<GridAction>,
        sharedPrefs: SharedPrefs,
        sortOptions: List<Sort>
): Observable<Unit> = actions
        .ofType(GridAction.SortSelection::class.java)
        .map { sortOptions.indexOf(it.sort) }
        .flatMap { sharedPrefs.writeSortPos(it) }
