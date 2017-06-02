package ch.berta.fabio.popularmovies.data

import ch.berta.fabio.popularmovies.data.dtos.Review
import ch.berta.fabio.popularmovies.data.dtos.Video
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.MovieEntity
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.ReviewEntity
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.VideoEntity
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.Movie
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MovieInfo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

sealed class GetFavMoviesResult {
    data class Success(val movies: List<MovieEntity>) : GetFavMoviesResult()
    object Failure : GetFavMoviesResult()
}

sealed class GetOnlMoviesResult {
    data class Success(val movies: List<Movie>) : GetOnlMoviesResult()
    object Failure : GetOnlMoviesResult()
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

data class MovieDetails(
        val isFav: Boolean,
        val dbId: Int,
        val title: String,
        val overview: String,
        val releaseDate: Date,
        val voteAverage: Double,
        val poster: String,
        val backdrop: String,
        val videos: List<Video>,
        val reviews: List<Review>
)

class MovieStorage @Inject constructor(val theMovieDbService: TheMovieDbService, val movieDb: MovieDb) {

    fun getFavMovies(): Observable<GetFavMoviesResult> = movieDb.movieDao().getAll()
            .map<GetFavMoviesResult> { GetFavMoviesResult.Success(it) }
            .onErrorReturn { GetFavMoviesResult.Failure }
            .toObservable()

    fun getOnlMovies(page: Int, sort: String): Observable<GetOnlMoviesResult> = theMovieDbService.loadMovies(page, sort)
            .map<GetOnlMoviesResult> { GetOnlMoviesResult.Success(it.movies) }
            .onErrorReturn { GetOnlMoviesResult.Failure }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()

    fun getMovieDetails(movieDbId: Int, fromFavList: Boolean): Observable<GetMovieDetailsResult> {
        val movieDetails =
                if (fromFavList) {
                    Flowable.combineLatest(
                            movieDb.movieDao().getByDbId(movieDbId),
                            movieDb.videoDao().getByMovieDbId(movieDbId),
                            movieDb.reviewDao().getByMovieDbId(movieDbId),
                            Function3<MovieEntity, List<VideoEntity>, List<ReviewEntity>, MovieDetails>
                            { movie, videos, reviews ->
                                MovieDetails(true, movie.dbId, movie.title, movie.overview, movie.releaseDate,
                                        movie.voteAverage, movie.poster, movie.backdrop,
                                        videos.map { Video(it.name, it.key, it.site, it.size, it.type) },
                                        reviews.map { Review(it.author, it.content, it.url) })
                            }
                    )
                            .toObservable()
                } else {
                    Observable.combineLatest(
                            theMovieDbService.loadMovieDetails(movieDbId).toObservable(),
                            movieDb.movieDao().existsByDbId(movieDbId)
                                    .map { it == 1 }
                                    .toObservable(),
                            BiFunction<MovieInfo, Boolean, MovieDetails> { details, fav ->
                                MovieDetails(fav, details.dbId, details.title, details.overview,
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

    fun saveMovieAsFav(movieDetails: MovieDetails): Observable<LocalDbWriteResult> = Observable.fromCallable {
        movieDb.beginTransaction()
        try {
            val movieEntity = MovieEntity(movieDetails.dbId, movieDetails.title, movieDetails.releaseDate,
                    movieDetails.voteAverage, movieDetails.overview, movieDetails.poster, movieDetails.backdrop)
            val movieId = movieDb.movieDao().insert(movieEntity)

            if (movieDetails.videos.isNotEmpty()) {
                val videoEntities = movieDetails.videos
                        .map { VideoEntity(movieId, it.name, it.key, it.site, it.size, it.type) }
                movieDb.videoDao().insertAll(videoEntities)
            }

            if (movieDetails.reviews.isNotEmpty()) {
                val reviewEntities = movieDetails.reviews
                        .map { ReviewEntity(movieId, it.author, it.content, it.url) }
                movieDb.reviewDao().insertAll(reviewEntities)
            }

            movieDb.setTransactionSuccessful()
        } finally {
            movieDb.endTransaction()
        }
    }
            .map<LocalDbWriteResult> { LocalDbWriteResult.SaveAsFav(true) }
            .onErrorReturn { LocalDbWriteResult.SaveAsFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun deleteMovieFromFav(movieDetails: MovieDetails): Observable<LocalDbWriteResult> = Observable.fromCallable {
        movieDb.movieDao().deleteByDbId(movieDetails.dbId)
    }
            .map<LocalDbWriteResult> { LocalDbWriteResult.DeleteFromFav(it > 0) }
            .onErrorReturn { LocalDbWriteResult.DeleteFromFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun updateFavMovie(movieDbId: Int): Observable<LocalDbWriteResult> = theMovieDbService.loadMovieDetails(movieDbId)
            .toObservable()
            .flatMap {
                Observable.fromCallable {
                    movieDb.beginTransaction()
                    try {
                        movieDb.movieDao().deleteByDbId(it.dbId)
                        movieDb.videoDao().deleteByMovieDbId(it.dbId)
                        movieDb.reviewDao().deleteByMovieDbId(it.dbId)
                        val movieEntity = MovieEntity(it.dbId, it.title, it.releaseDate, it.voteAverage, it.overview,
                                it.poster, it.backdrop)
                        val movieId = movieDb.movieDao().insert(movieEntity)

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
            .map<LocalDbWriteResult> { LocalDbWriteResult.UpdateFav(true) }
            .onErrorReturn { LocalDbWriteResult.DeleteFromFav(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}