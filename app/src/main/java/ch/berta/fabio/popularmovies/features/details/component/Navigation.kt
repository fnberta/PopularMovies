package ch.berta.fabio.popularmovies.features.details.component

import android.content.Intent
import android.net.Uri
import ch.berta.fabio.popularmovies.effects.NavigationTarget
import rx.Observable

const val RS_FAV_DELETED = 3
const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="

fun navigate(actions: Observable<DetailsAction>): Observable<NavigationTarget> {
    val favDelete = actions
            .ofType(DetailsAction.FavDelete::class.java)
            .skipUntil(actions.ofType(DetailsAction.DetailsFavLoad::class.java))
            .map { NavigationTarget.Finish(RS_FAV_DELETED) }
    val videoClick = actions
            .ofType(DetailsAction.VideoClick::class.java)
            .map { "$YOUTUBE_BASE_URL${it.videoViewModel.key}" }
            .map { NavigationTarget.Action(Intent.ACTION_VIEW, Uri.parse(it)) }

    val navigators = listOf(favDelete, videoClick)
    return Observable.merge(navigators)
}