package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.data.dtos.YOU_TUBE
import io.reactivex.Observable

fun intention(sources: DetailsSources): Observable<DetailsAction> {
    val favClicks = sources.uiEvents.favClicks
            .map { DetailsAction.FavClick }
    val updateSwipes = sources.uiEvents.updateSwipes
            .map { DetailsAction.UpdateSwipe }
    val videoClick = sources.uiEvents.videoClicks
            .filter { it.site == YOU_TUBE }
            .map { DetailsAction.VideoClick(it) }

    val actions = listOf(favClicks, updateSwipes, videoClick)
    return Observable.merge(actions)
}