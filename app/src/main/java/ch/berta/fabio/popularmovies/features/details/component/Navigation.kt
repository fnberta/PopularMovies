/*
 * Copyright (c) 2017 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies.features.details.component

import android.content.Intent
import android.net.Uri
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import io.reactivex.Observable

const val RS_REMOVE_FROM_FAV = 3
const val RS_DATA_MOVIE_ID = "RS_DATA_MOVIE_ID"
const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="

fun navigationTargets(
        actions: Observable<DetailsAction>,
        detailsArgs: DetailsArgs
): Observable<NavigationTarget> {
    val favDelete = actions
            .ofType(DetailsAction.FavClick::class.java)
            .filter { detailsArgs.fromFavList }
            .map { NavigationTarget.Finish(RS_REMOVE_FROM_FAV, mapOf(RS_DATA_MOVIE_ID to detailsArgs.id)) }
    val videoClick = actions
            .ofType(DetailsAction.VideoClick::class.java)
            .map { "$YOUTUBE_BASE_URL${it.videoViewModel.key}" }
            .map { NavigationTarget.Action(Intent.ACTION_VIEW, Uri.parse(it)) }

    val navigationTargets = listOf(favDelete, videoClick)
    return Observable.merge(navigationTargets)
}