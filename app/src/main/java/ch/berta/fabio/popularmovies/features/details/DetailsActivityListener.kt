package ch.berta.fabio.popularmovies.features.details

import ch.berta.fabio.popularmovies.features.grid.component.GridViewState
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import com.jakewharton.rxrelay.BehaviorRelay
import rx.Observable

interface DetailsActivityListener : BaseFragment.ActivityListener {
    var state: Observable<GridViewState>
    val starMovieClicks: BehaviorRelay<Int>
    val expandPlotClicks: BehaviorRelay<Int>
}