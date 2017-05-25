package ch.berta.fabio.popularmovies.features.details.loaders

import android.content.Context
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.RxLoader
import ch.berta.fabio.popularmovies.data.services.MovieService
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class DetailsOnlLoader(
        context: Context,
        movieService: MovieService,
        movieDbId: Int
) : RxLoader<MovieDetails>(context) {

    override val observable: Observable<MovieDetails> by lazy {
        movieService.loadMovieDetails(movieDbId, context.getString(R.string.movie_db_key),
                "reviews,videos")
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}