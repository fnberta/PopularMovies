package ch.berta.fabio.popularmovies.features.grid.component

import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.grid.Sort
import io.reactivex.Observable

fun sharedPrefWrites(
        actions: Observable<GridAction>,
        sharedPrefs: SharedPrefs,
        sortOptions: List<Sort>
): Observable<Unit> {
    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)
            .map { sortOptions.indexOf(it.sort) }
            .flatMap { sharedPrefs.writeSortPos(it) }

    val sharedPrefWrites = listOf(sortSelections)
    return Observable.merge(sharedPrefWrites)
}

fun localMovieDbWrites(
        actions: Observable<GridAction>,
        movieStorage: MovieStorage
): Observable<LocalDbWriteResult> {
    val favDelete = actions
            .ofType(GridAction.FavDelete::class.java)
            .flatMap { movieStorage.deleteMovieFromFav(it.movieId) }

    val dbWrites = listOf(favDelete)
    return Observable.merge(dbWrites)
}