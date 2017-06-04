/*
 * Copyright (c) 2017 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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