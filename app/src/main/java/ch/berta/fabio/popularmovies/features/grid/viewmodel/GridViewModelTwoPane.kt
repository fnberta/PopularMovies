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

package ch.berta.fabio.popularmovies.features.grid.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.component.DetailsUiEvents
import ch.berta.fabio.popularmovies.features.details.vdos.rows.DetailsVideoRowViewData
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModel
import ch.berta.fabio.popularmovies.features.details.viewmodel.getDetailsState
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.component.GridUiEvents
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.toLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ch.berta.fabio.popularmovies.features.details.component.main as detailsMain
import ch.berta.fabio.popularmovies.features.grid.component.main as gridMain

sealed class MoviesState {
    data class Grid(val value: GridState) : MoviesState()
    data class Details(val value: DetailsState) : MoviesState()
}

class GridViewModelTwoPane(
        sharedPrefs: SharedPrefs,
        movieStorage: MovieStorage,
        sortOptions: List<Sort>
) : ViewModel(), GridViewModel, DetailsViewModel {

    override val transientClears: PublishRelay<Unit> = PublishRelay.create()
    override val activityResults: PublishRelay<ActivityResult> = PublishRelay.create()
    override val sortSelections: PublishRelay<Int> = PublishRelay.create()
    override val movieSelections: PublishRelay<SelectedMovie> = PublishRelay.create()
    override val loadMore: PublishRelay<Unit> = PublishRelay.create()
    override val refreshSwipes: PublishRelay<Unit> = PublishRelay.create()

    override val updateSwipes: PublishRelay<Unit> = PublishRelay.create()
    override val favClicks: PublishRelay<Unit> = PublishRelay.create()
    override val videoClicks: PublishRelay<DetailsVideoRowViewData> = PublishRelay.create()

    override val state: LiveData<MoviesState>

    init {
        val gridUiEvents = GridUiEvents(transientClears, activityResults, sortSelections, movieSelections, loadMore,
                refreshSwipes)
        val detailsUiEvents = DetailsUiEvents(transientClears, movieSelections, sortSelections, updateSwipes, favClicks,
                videoClicks)
        state = Observable.merge(
                getGridState(gridUiEvents, sharedPrefs, movieStorage, sortOptions).map { MoviesState.Grid(it) },
                getDetailsState(detailsUiEvents, movieStorage).map { MoviesState.Details(it) }
        ).toLiveData()
    }
}