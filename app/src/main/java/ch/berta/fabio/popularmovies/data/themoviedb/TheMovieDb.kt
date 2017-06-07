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

package ch.berta.fabio.popularmovies.data.themoviedb

import ch.berta.fabio.popularmovies.data.dtos.Movie
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MovieInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class LoadMoviesResult {
    data class Success(val movies: List<Movie>) : LoadMoviesResult()
    object Failure : LoadMoviesResult()
}

sealed class LoadMovieDetailsResult {
    data class Success(val movieInfo: MovieInfo) : LoadMovieDetailsResult()
    object Failure : LoadMovieDetailsResult()
}

class TheMovieDb @Inject constructor(private val theMovieDbService: TheMovieDbService) {

    fun movies(page: Int, sort: String): Observable<LoadMoviesResult> = theMovieDbService.loadMovies(page, sort)
            .map<LoadMoviesResult> { LoadMoviesResult.Success(it.movies) }
            .onErrorReturn { LoadMoviesResult.Failure }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()

    fun movieDetails(movieId: Int): Observable<LoadMovieDetailsResult> = theMovieDbService.loadMovieInfo(movieId)
            .map<LoadMovieDetailsResult> { LoadMovieDetailsResult.Success(it) }
            .onErrorReturn { LoadMovieDetailsResult.Failure }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
}
