package ch.berta.fabio.popularmovies.features.grid.view

import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import com.jakewharton.rxrelay.BehaviorRelay

interface GridOnlActivityListener : GridActivityListener {
    val moviesOnl: BehaviorRelay<List<Movie>>
    val refreshSwipes: BehaviorRelay<Unit>
}