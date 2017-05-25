package ch.berta.fabio.popularmovies.features.common

import android.support.annotation.StringRes

data class SnackbarMessage(
        val show: Boolean,
        @StringRes val message: Int = -1
)
