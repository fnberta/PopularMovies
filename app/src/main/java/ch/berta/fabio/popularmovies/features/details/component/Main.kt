package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.vdo.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import ch.berta.fabio.popularmovies.log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

data class DetailsSources(
        val uiEvents: DetailsUiEvents,
        val movieStorage: MovieStorage,
        val localDbWriteResults: Observable<LocalDbWriteResult>
)

data class DetailsUiEvents(
        val snackbarShown: PublishRelay<Unit> = PublishRelay.create(),
        val updateSwipes: PublishRelay<Unit> = PublishRelay.create(),
        val favClicks: PublishRelay<Unit> = PublishRelay.create(),
        val videoClicks: PublishRelay<DetailsVideoRowViewData> = PublishRelay.create()
)

sealed class DetailsAction {
    object UpdateSwipe : DetailsAction()
    object FavClick : DetailsAction()
    data class VideoClick(val videoViewModel: DetailsVideoRowViewData) : DetailsAction()
}

sealed class DetailsSink {
    data class State(val state: DetailsState) : DetailsSink()
    data class Navigation(val target: NavigationTarget) : DetailsSink()
    data class LocalDbWrite(val result: LocalDbWriteResult) : DetailsSink()
}

fun main(sources: DetailsSources, detailsArgs: DetailsArgs): Observable<DetailsSink> = intention(sources)
        .log("action")
        .publish {
            val getMovieDetails = sources.movieStorage.getMovieDetails(detailsArgs.movieId, detailsArgs.fromFavList).share()

            val state = model(it, getMovieDetails, sources.localDbWriteResults, detailsArgs)
                    .map { DetailsSink.State(it) }
            val navigationTargets = navigationTargets(it, sources.localDbWriteResults, detailsArgs.fromFavList)
                    .map { DetailsSink.Navigation(it) }
            val localDbWrites = localMovieDbWrites(it, sources.movieStorage, getMovieDetails, detailsArgs.movieId)
                    .map { DetailsSink.LocalDbWrite(it) }

            Observable.merge(state, navigationTargets, localDbWrites)
        }
        .share()
