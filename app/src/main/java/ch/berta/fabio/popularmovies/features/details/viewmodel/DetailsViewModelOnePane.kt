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

package ch.berta.fabio.popularmovies.features.details.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.component.DetailsSources
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.component.DetailsUiEvents
import ch.berta.fabio.popularmovies.features.details.component.main
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.features.grid.viewmodel.MoviesState
import ch.berta.fabio.popularmovies.toLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

fun getDetailsState(uiEvents: DetailsUiEvents, movieStorage: MovieStorage): Observable<DetailsState> {
    val sources = DetailsSources(uiEvents, movieStorage)
    val initialState = DetailsState()
    return main(sources, initialState)
}

class DetailsViewModelOnePane(movieStorage: MovieStorage) : ViewModel(), DetailsViewModel {

    override val transientClears: PublishRelay<Unit> = PublishRelay.create()
    override val movieSelections: PublishRelay<SelectedMovie> = PublishRelay.create()
    override val sortSelections: PublishRelay<Int> = PublishRelay.create()
    override val updateSwipes: PublishRelay<Unit> = PublishRelay.create()
    override val favClicks: PublishRelay<Unit> = PublishRelay.create()
    override val videoClicks: PublishRelay<DetailsVideoRowViewData> = PublishRelay.create()

    override val state: LiveData<MoviesState>

    init {
        val uiEvents = DetailsUiEvents(transientClears, movieSelections, sortSelections, updateSwipes, favClicks,
                videoClicks)

        state = getDetailsState(uiEvents, movieStorage)
                .map<MoviesState> { MoviesState.Details(it) }
                .toLiveData()
    }
}