package ch.berta.fabio.popularmovies.features.grid

import ch.berta.fabio.popularmovies.features.grid.component.GridViewState
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import com.jakewharton.rxrelay.BehaviorRelay
import rx.Observable

interface GridActivityListener : BaseFragment.ActivityListener {
    var state: Observable<GridViewState>
    val movieClicks: BehaviorRelay<Int>
    val loadMore: BehaviorRelay<Unit>
}