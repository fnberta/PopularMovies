package ch.berta.fabio.popularmovies.features.details.view

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import com.jakewharton.rxrelay.BehaviorRelay

interface DetailsOnlActivityListener : DetailsActivityListener {
    val detailsOnl: BehaviorRelay<MovieDetails>
    val detailsOnlId: BehaviorRelay<Maybe<Cursor>>
}