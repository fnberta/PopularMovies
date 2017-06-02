package ch.berta.fabio.popularmovies.features.details.component

import android.content.Intent
import android.net.Uri
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import io.reactivex.Observable

const val RS_REMOVE_FROM_FAV = 3
const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="

fun navigationTargets(
        actions: Observable<DetailsAction>,
        localDbWriteResult: Observable<LocalDbWriteResult>,
        fromFavList: Boolean
): Observable<NavigationTarget> {
    val favDelete = localDbWriteResult
            .ofType(LocalDbWriteResult.DeleteFromFav::class.java)
            .filter { fromFavList }
            .map { NavigationTarget.Finish(RS_REMOVE_FROM_FAV) }
    val videoClick = actions
            .ofType(DetailsAction.VideoClick::class.java)
            .map { "$YOUTUBE_BASE_URL${it.videoViewModel.key}" }
            .map { NavigationTarget.Action(Intent.ACTION_VIEW, Uri.parse(it)) }

    val navigationTargets = listOf(favDelete, videoClick)
    return Observable.merge(navigationTargets)
}