package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.data.services.dtos.YOU_TUBE
import rx.Observable

fun intention(sources: DetailsSources): Observable<DetailsAction> {
    val favClicks = sources.viewEvents.favClicks
            .map { DetailsAction.FavClick }
    val updateSwipes = sources.viewEvents.updateSwipes
            .map { DetailsAction.UpdateSwipe }
    val videoClick = sources.viewEvents.videoClicks
            .filter { it.site == YOU_TUBE }
            .map { DetailsAction.VideoClick(it) }

    val onlLoad = sources.dataLoadEvents.detailsOnl
            .map { DetailsAction.DetailsOnlLoad(it) }
    val onlLoadIsFav = sources.dataLoadEvents.detailsOnlId
            .map { DetailsAction.DetailsOnlIdLoad(it) }
    val favLoad = sources.dataLoadEvents.detailsFav
            .map { DetailsAction.DetailsFavLoad(it) }

    val favSave = sources.persistenceEvents.contentProviderRes
            .filter { it.id == SAVE_AS_FAV }
            .map { DetailsAction.FavSave }
    val favDelete = sources.persistenceEvents.contentProviderRes
            .filter { it.id == DELETE_FAV }
            .map { DetailsAction.FavDelete }

    val actions = listOf(favClicks, updateSwipes, videoClick, onlLoad, onlLoadIsFav, favLoad,
            favSave, favDelete)
    return Observable.merge(actions)
}