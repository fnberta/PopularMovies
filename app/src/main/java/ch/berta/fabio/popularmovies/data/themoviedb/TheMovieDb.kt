package ch.berta.fabio.popularmovies.data.themoviedb

import ch.berta.fabio.popularmovies.data.themoviedb.dtos.Movie
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

    fun movieDetails(movieId: Int): Observable<LoadMovieDetailsResult> = theMovieDbService.loadMovieDetails(movieId)
            .map<LoadMovieDetailsResult> { LoadMovieDetailsResult.Success(it) }
            .onErrorReturn { LoadMovieDetailsResult.Failure }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
}
