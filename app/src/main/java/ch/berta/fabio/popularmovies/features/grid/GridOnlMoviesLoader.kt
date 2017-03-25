package ch.berta.fabio.popularmovies.features.grid

import android.content.Context
import ch.berta.fabio.popularmovies.data.RxLoader
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class GridOnlMoviesLoader(
        context: Context,
        val movieRepo: MovieRepository
) : RxLoader<List<Movie>>(context) {

    var sort: String = ""
    var page: Int = 0

    override fun getObservable(): Observable<List<Movie>> = movieRepo.getMoviesOnline(page, sort)
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}