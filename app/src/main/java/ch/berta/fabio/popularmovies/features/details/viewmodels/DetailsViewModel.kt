package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.BR

class DetailsViewModel : BaseObservable() {
    @get:Bindable var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @get:Bindable var backdropPath: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.backdropPath)
        }

    @get:Bindable var favoured: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.favoured)
        }
}