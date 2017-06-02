package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs

class DetailsViewModelFactory(
        val movieStorage: MovieStorage,
        val detailsArgs: DetailsArgs
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetailsViewModel(movieStorage, detailsArgs) as T
    }
}