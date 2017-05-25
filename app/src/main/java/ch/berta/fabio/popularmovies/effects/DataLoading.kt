package ch.berta.fabio.popularmovies.effects

import android.os.Parcelable

const val KEY_LOADER_ARGS = "KEY_LOADER_ARGS"

data class LoaderTarget(
        val key: Int,
        val args: Parcelable? = null
)