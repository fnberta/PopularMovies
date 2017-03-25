package ch.berta.fabio.popularmovies.features.grid

import ch.berta.fabio.popularmovies.features.grid.GridActivityListener
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import com.jakewharton.rxrelay.BehaviorRelay

interface GridFavActivityListener : GridActivityListener {
    val component: GridComponent
    val moviesFav: BehaviorRelay<Sequence<Map<String, Any?>>>
}