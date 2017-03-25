package ch.berta.fabio.popularmovies.features.grid.rows

import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingRow
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxrelay.BehaviorRelay

class BindingMovieRow(
        binding: RowMovieBinding,
        movieClicks: BehaviorRelay<Int>
) : BaseBindingRow<RowMovieBinding>(binding) {

    init {
        binding.root.clicks()
                .map { adapterPosition }
                .subscribe(movieClicks)
    }
}