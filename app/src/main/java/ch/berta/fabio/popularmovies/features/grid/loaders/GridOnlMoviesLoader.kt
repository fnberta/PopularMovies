package ch.berta.fabio.popularmovies.features.grid.loaders

import android.content.Context
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.RxLoader
import ch.berta.fabio.popularmovies.data.services.MovieService
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class GridOnlMoviesLoader(
        context: Context,
        movieService: MovieService
) : RxLoader<List<Movie>>(context) {

    override val observable: Observable<List<Movie>> by lazy {
        Observable.range(1, page)
                .concatMap {
                    movieService.loadMoviePosters(it, sort,
                            context.getString(R.string.movie_db_key))
                            .flatMapObservable { Observable.from(it.movies) }
                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    var sort: String = ""
    var page: Int = 0
}