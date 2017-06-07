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

package ch.berta.fabio.popularmovies.data

import ch.berta.fabio.popularmovies.data.dtos.Movie
import ch.berta.fabio.popularmovies.data.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.dtos.Review
import ch.berta.fabio.popularmovies.data.dtos.Video
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.MovieEntity
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.ReviewEntity
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.VideoEntity
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MovieInfo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class GetMoviesResult {
    data class Success(val movies: List<Movie>) : GetMoviesResult()
    object Failure : GetMoviesResult()
}

sealed class GetMovieDetailsResult {
    data class Success(val movieDetails: MovieDetails) : GetMovieDetailsResult()
    object Failure : GetMovieDetailsResult()
}

sealed class LocalDbWriteResult {
    data class SaveAsFav(val successful: Boolean) : LocalDbWriteResult()
    data class DeleteFromFav(val successful: Boolean) : LocalDbWriteResult()
    data class UpdateFav(val successful: Boolean) : LocalDbWriteResult()
}

class MovieStorage @Inject constructor(val theMovieDbService: TheMovieDbService, val movieDb: MovieDb) {

    fun getFavMovies(): Observable<GetMoviesResult> = movieDb.movieDao().getAll().toObservable()
            .map {
                it.map {
                    Movie(it.id, it.backdrop, it.overview, it.releaseDate, it.poster, it.title, it.voteAverage)
                }
            }
            .map<GetMoviesResult> { GetMoviesResult.Success(it) }
            .onErrorReturn { GetMoviesResult.Failure }

    fun getOnlMovies(page: Int, sort: String, fetchAllPages: Boolean): Observable<GetMoviesResult> =
            if (fetchAllPages) {
                Observable.range(1, page)
                        .concatMap {
                            theMovieDbService.loadMovies(it, sort)
                                    .flatMapObservable { Observable.fromIterable(it.movies) }
                        }
                        .toList()

            } else {
                theMovieDbService.loadMovies(page, sort)
                        .map { it.movies }
            }
                    .toObservable()
                    .map<GetMoviesResult> { GetMoviesResult.Success(it) }
                    .onErrorReturn { GetMoviesResult.Failure }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getMovieDetails(movieId: Int, fromFavList: Boolean): Observable<GetMovieDetailsResult> {
        val movieDetails =
                if (fromFavList) {
                    Flowable.combineLatest(
                            movieDb.movieDao().getById(movieId),
                            movieDb.videoDao().getByMovieId(movieId),
                            movieDb.reviewDao().getByMovieId(movieId),
                            Function3<MovieEntity, List<VideoEntity>, List<ReviewEntity>, MovieDetails>
                            { movie, videos, reviews ->
                                MovieDetails(true, movie.id, movie.title,
                                        movie.overview, movie.releaseDate,
                                        movie.voteAverage, movie.poster, movie.backdrop,
                                        videos.map { Video(it.name, it.key, it.site, it.size, it.type) },
                                        reviews.map { Review(it.author, it.content, it.url) })
                            }
                    )
                            .toObservable()
                } else {
                    Observable.combineLatest(
                            theMovieDbService.loadMovieInfo(movieId).toObservable(),
                            movieDb.movieDao().existsById(movieId)
                                    .map { it == 1 }
                                    .toObservable(),
                            BiFunction<MovieInfo, Boolean, MovieDetails> { details, fav ->
                                MovieDetails(fav, details.id, details.title,
                                        details.overview,
                                        details.releaseDate, details.voteAverage, details.poster, details.backdrop,
                                        details.videosPage.videos, details.reviewsPage.reviews)
                            }
                    )
                }

        return movieDetails
                .map<GetMovieDetailsResult> { GetMovieDetailsResult.Success(it) }
                .onErrorReturn { GetMovieDetailsResult.Failure }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun saveMovieAsFav(movieDetails: MovieDetails): Observable<LocalDbWriteResult.SaveAsFav> = Observable.fromCallable {
        movieDb.beginTransaction()
        try {
            val movieEntity = MovieEntity(movieDetails.id, movieDetails.title, movieDetails.releaseDate,
                    movieDetails.voteAverage, movieDetails.overview, movieDetails.poster, movieDetails.backdrop)
            movieDb.movieDao().insert(movieEntity)

            if (movieDetails.videos.isNotEmpty()) {
                val videoEntities = movieDetails.videos
                        .map { VideoEntity(movieDetails.id, it.name, it.key, it.site, it.size, it.type) }
                movieDb.videoDao().insertAll(videoEntities)
            }

            if (movieDetails.reviews.isNotEmpty()) {
                val reviewEntities = movieDetails.reviews
                        .map { ReviewEntity(movieDetails.id, it.author, it.content, it.url) }
                movieDb.reviewDao().insertAll(reviewEntities)
            }

            movieDb.setTransactionSuccessful()
        } finally {
            movieDb.endTransaction()
        }
    }
            .map { LocalDbWriteResult.SaveAsFav(true) }
            .onErrorReturn { LocalDbWriteResult.SaveAsFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun deleteMovieFromFav(movieId: Int): Observable<LocalDbWriteResult.DeleteFromFav> = Observable.fromCallable {
        movieDb.movieDao().deleteById(movieId)
    }
            .map { LocalDbWriteResult.DeleteFromFav(it > 0) }
            .onErrorReturn { LocalDbWriteResult.DeleteFromFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun updateFavMovie(movieId: Int): Observable<LocalDbWriteResult.UpdateFav> = theMovieDbService.loadMovieInfo(
            movieId)
            .toObservable()
            .flatMap {
                Observable.fromCallable {
                    movieDb.beginTransaction()
                    try {
                        val movieEntity = MovieEntity(it.id, it.title, it.releaseDate, it.voteAverage, it.overview,
                                it.poster, it.backdrop)
                        movieDb.movieDao().update(movieEntity)
                        movieDb.videoDao().deleteByMovieId(it.id)
                        movieDb.reviewDao().deleteByMovieId(it.id)

                        if (it.videosPage.videos.isNotEmpty()) {
                            val videoEntities = it.videosPage.videos
                                    .map { VideoEntity(movieId, it.name, it.key, it.site, it.size, it.type) }
                            movieDb.videoDao().insertAll(videoEntities)
                        }

                        if (it.reviewsPage.reviews.isNotEmpty()) {
                            val reviewEntities = it.reviewsPage.reviews
                                    .map { ReviewEntity(movieId, it.author, it.content, it.url) }
                            movieDb.reviewDao().insertAll(reviewEntities)
                        }

                        movieDb.setTransactionSuccessful()
                    } finally {
                        movieDb.endTransaction()
                    }
                }
            }
            .map { LocalDbWriteResult.UpdateFav(true) }
            .onErrorReturn { LocalDbWriteResult.UpdateFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}