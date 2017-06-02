package ch.berta.fabio.popularmovies.features.common

import android.support.annotation.StringRes
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class SnackbarMessage(
        val show: Boolean,
        @StringRes val message: Int = -1
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelSnackbarMessage.CREATOR
    }
}
