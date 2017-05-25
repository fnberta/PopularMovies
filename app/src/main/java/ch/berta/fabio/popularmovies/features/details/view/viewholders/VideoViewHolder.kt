package ch.berta.fabio.popularmovies.features.details.view.viewholders

import ch.berta.fabio.popularmovies.databinding.RowDetailsVideoBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder
import com.jakewharton.rxbinding.view.clicks
import rx.Observable

/**
 * Provides a [RecyclerView] row that displays videos (e.g. trailers) about a movie.
 *
 */
class VideoViewHolder(binding: RowDetailsVideoBinding) : BaseBindingViewHolder<RowDetailsVideoBinding>(
        binding) {

    val clicks: Observable<Int> by lazy {
        binding.root.clicks()
                .map { adapterPosition }
    }
}