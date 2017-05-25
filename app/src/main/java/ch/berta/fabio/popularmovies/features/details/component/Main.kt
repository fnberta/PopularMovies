package ch.berta.fabio.popularmovies.features.details.component

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.effects.ContentProviderResult
import ch.berta.fabio.popularmovies.effects.ContentProviderTarget
import ch.berta.fabio.popularmovies.effects.NavigationTarget
import ch.berta.fabio.popularmovies.extensions.debug
import ch.berta.fabio.popularmovies.features.details.viewmodels.rows.DetailsVideoRowViewModel
import rx.Observable

data class DetailsSources(
        val viewEvents: DetailsViewEvents,
        val dataLoadEvents: DetailsDataLoadEvents,
        val persistenceEvents: PersistenceEvents
)

data class DetailsViewEvents(
        val updateSwipes: Observable<Unit>,
        val favClicks: Observable<Unit>,
        val videoClicks: Observable<DetailsVideoRowViewModel>
)

data class DetailsDataLoadEvents(
        val detailsFav: Observable<Maybe<Cursor>>,
        val detailsOnl: Observable<MovieDetails>,
        val detailsOnlId: Observable<Maybe<Cursor>>
)

data class PersistenceEvents(val contentProviderRes: Observable<ContentProviderResult>)

sealed class DetailsAction {
    data class DetailsOnlLoad(val movieDetails: MovieDetails) : DetailsAction()
    data class DetailsOnlIdLoad(val detailsOnlIdResult: Maybe<Cursor>) : DetailsAction()
    data class DetailsFavLoad(val detailsFavLoad: Maybe<Cursor>) : DetailsAction()
    object UpdateSwipe : DetailsAction()
    object FavClick : DetailsAction()
    data class VideoClick(val videoViewModel: DetailsVideoRowViewModel) : DetailsAction()
    object FavSave : DetailsAction()
    object FavDelete : DetailsAction()
}

data class DetailsSinks(
        val state: Observable<DetailsState>,
        val navigation: Observable<NavigationTarget>,
        val contentProviderOps: Observable<ContentProviderTarget>
)

fun main(initialState: DetailsState, sources: DetailsSources): DetailsSinks {
    val actions = intention(sources)
            .debug("action")
            .share()
    val state = model(initialState, actions)
            .share()
    val contentProviderOps = persist(actions)
            .share()
    val navigation = navigate(actions)
            .share()

    return DetailsSinks(state, navigation, contentProviderOps)
}
