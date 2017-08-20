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

import android.support.annotation.StringRes
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsRowViewData
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.log
import io.reactivex.Observable

sealed class DetailsAction {
    object TransientClear : DetailsAction()
    data class MovieSelected(val selectedMovie: SelectedMovie) : DetailsAction()
    object SortSelection : DetailsAction()
    object UpdateSwipe : DetailsAction()
    object FavClick : DetailsAction()
    data class VideoClick(val videoViewModel: DetailsVideoRowViewData) : DetailsAction()
}

data class DetailsSources(val uiEvents: DetailsUiEvents, val movieStorage: MovieStorage)

data class DetailsUiEvents(
        val transientClears: Observable<Unit>,
        val movieSelections: Observable<SelectedMovie>,
        val sortSelections: Observable<Int>,
        val updateSwipes: Observable<Unit>,
        val favClicks: Observable<Unit>,
        val videoClicks: Observable<DetailsVideoRowViewData>
)

data class DetailsState(
        val updateEnabled: Boolean = false,
        val updating: Boolean = false,
        val title: String = "",
        val backdrop: String? = "",
        val favoured: Boolean = false,
        val details: List<DetailsRowViewData> = emptyList(),
        val movieDeletedFromFavScreen: Boolean = false,
        @StringRes val message: Int? = null,
        val selectedVideoUrl: String? = null
)

fun main(sources: DetailsSources, initialState: DetailsState): Observable<DetailsState> = intention(sources)
        .log("details action")
        .publish { model(initialState, it, sources.movieStorage) }
        .share()
