package ch.berta.fabio.popularmovies.features.details.view

import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsSinks
import ch.berta.fabio.popularmovies.features.details.viewmodels.rows.DetailsVideoRowViewModel
import com.jakewharton.rxrelay.BehaviorRelay

interface DetailsActivityListener : BaseFragment.ActivityListener {
    var sinks: DetailsSinks
    val updateSwipes: BehaviorRelay<Unit>
    val videoClicks: BehaviorRelay<DetailsVideoRowViewModel>
}