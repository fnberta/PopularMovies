package ch.berta.fabio.popularmovies.features.grid.view

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import com.jakewharton.rxrelay.BehaviorRelay

interface GridFavActivityListener : GridActivityListener {
    val moviesFav: BehaviorRelay<Maybe<Cursor>>
}