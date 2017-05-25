package ch.berta.fabio.popularmovies.features.grid.view

import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.component.GridSinks
import ch.berta.fabio.popularmovies.features.grid.component.SelectedMovie
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import com.jakewharton.rxrelay.BehaviorRelay

interface GridActivityListener : BaseFragment.ActivityListener {
    var sinks: GridSinks
    val component: GridComponent
    val movieClicks: BehaviorRelay<SelectedMovie>
    val loadMore: BehaviorRelay<Unit>
}