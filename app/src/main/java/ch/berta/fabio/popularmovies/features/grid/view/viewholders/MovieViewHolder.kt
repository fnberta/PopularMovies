package ch.berta.fabio.popularmovies.features.grid.view.viewholders

import android.view.View
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder
import com.jakewharton.rxbinding.view.clicks
import rx.Observable

data class MovieRowClick(
        val position: Int,
        val posterView: View
)

class MovieViewHolder(
        binding: RowMovieBinding
) : BaseBindingViewHolder<RowMovieBinding>(binding) {

    val clicks: Observable<MovieRowClick> by lazy {
        binding.root.clicks()
                .map {
                    MovieRowClick(adapterPosition, binding.ivPoster)
                }
    }
}