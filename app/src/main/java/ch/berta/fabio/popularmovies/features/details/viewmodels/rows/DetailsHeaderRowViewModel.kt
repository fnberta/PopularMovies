package ch.berta.fabio.popularmovies.features.details.viewmodels.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.features.common.viewmodels.HeaderRowViewModel

data class DetailsHeaderRowViewModel(
        @get:Bindable override val header: Int,
        override val viewType: Int = R.layout.row_header
) : BaseObservable(), DetailsRowViewModel, HeaderRowViewModel