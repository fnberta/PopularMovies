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

import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import ch.berta.fabio.popularmovies.log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

data class DetailsSources(
        val uiEvents: DetailsUiEvents,
        val movieStorage: MovieStorage
)

data class DetailsUiEvents(
        val snackbarShown: PublishRelay<Unit> = PublishRelay.create(),
        val updateSwipes: PublishRelay<Unit> = PublishRelay.create(),
        val favClicks: PublishRelay<Unit> = PublishRelay.create(),
        val videoClicks: PublishRelay<DetailsVideoRowViewData> = PublishRelay.create()
)

sealed class DetailsAction {
    object SnackbarShown : DetailsAction()
    object UpdateSwipe : DetailsAction()
    object FavClick : DetailsAction()
    data class VideoClick(val videoViewModel: DetailsVideoRowViewData) : DetailsAction()
}

sealed class DetailsSink {
    data class State(val state: DetailsState) : DetailsSink()
    data class Navigation(val target: NavigationTarget) : DetailsSink()
}

fun main(sources: DetailsSources, detailsArgs: DetailsArgs): Observable<DetailsSink> = intention(sources)
        .log("action")
        .publish {
            val state = model(it, sources.movieStorage, detailsArgs)
                    .map { DetailsSink.State(it) }
            val navigationTargets = navigationTargets(it, detailsArgs)
                    .map { DetailsSink.Navigation(it) }

            Observable.merge(state, navigationTargets)
        }
        .share()
