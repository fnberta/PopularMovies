package ch.berta.fabio.popularmovies.features.details.view

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import com.jakewharton.rxrelay.BehaviorRelay

interface DetailsFavActivityListener : DetailsActivityListener {
    val detailsFav: BehaviorRelay<Maybe<Cursor>>
}