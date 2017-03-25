package ch.berta.fabio.popularmovies.features.grid

import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.features.grid.GridActivityListener
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import com.jakewharton.rxrelay.BehaviorRelay

interface GridOnlActivityListener : GridActivityListener {
    val component: GridComponent
    val moviesOnl: BehaviorRelay<List<Movie>>
    val refreshSwipes: BehaviorRelay<Unit>
}