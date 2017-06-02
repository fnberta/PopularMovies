package ch.berta.fabio.popularmovies.features.details.vdo.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.features.common.vdos.HeaderRowViewData

data class DetailsHeaderRowViewData(
        @get:Bindable override val header: Int,
        override val viewType: Int = R.layout.row_header
) : BaseObservable(), DetailsRowViewData, HeaderRowViewData