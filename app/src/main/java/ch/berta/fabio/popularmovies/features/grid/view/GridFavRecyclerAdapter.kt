/*
 * Copyright (c) 2015 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies.features.grid.view

import android.database.Cursor
import android.provider.BaseColumns
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.extensions.getIntFromColumn
import ch.berta.fabio.popularmovies.extensions.getLongFromColumn
import ch.berta.fabio.popularmovies.extensions.getStringFromColumn
import ch.berta.fabio.popularmovies.features.grid.component.SelectedMovie
import ch.berta.fabio.popularmovies.features.grid.view.viewholders.MovieViewHolder
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridRowViewModel
import com.jakewharton.rxrelay.BehaviorRelay
import java.text.DateFormat
import java.util.*

/**
 * Provides the adapter for a movie poster images grid.
 */
class GridFavRecyclerAdapter(
        val movieClicks: BehaviorRelay<SelectedMovie>,
        val posterHeight: Int,
        var data: Cursor?
) : RecyclerView.Adapter<MovieViewHolder>() {

    private val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder =
            RowMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false).let {
                MovieViewHolder(it).apply {
                    clicks
                            .map { SelectedMovie.Fav(getItemId(it.position), it.posterView) }
                            .subscribe(movieClicks)
                }
            }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        data?.let {
            if (!it.moveToPosition(position)) {
                throw IllegalStateException("couldn't move cursor to position $position")
            }

            val dbId = it.getIntFromColumn(MovieContract.Movie.COLUMN_DB_ID)
            val title = it.getStringFromColumn(MovieContract.Movie.COLUMN_TITLE)
            val date = Date(it.getLongFromColumn(MovieContract.Movie.COLUMN_RELEASE_DATE))
            val poster = it.getStringFromColumn(MovieContract.Movie.COLUMN_POSTER)

            holder.binding.viewModel = GridRowViewModel(dbId, title, dateFormatter.format(date),
                    poster, posterHeight)
            holder.binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int = data?.count ?: 0

    override fun getItemId(position: Int): Long = data?.let {
        if (it.moveToPosition(position)) it.getLongFromColumn(BaseColumns._ID)
        else RecyclerView.NO_ID
    } ?: RecyclerView.NO_ID

    fun swapData(newData: Maybe<Cursor>) {
        if (newData == data) {
            return
        }

        when (newData) {
            is Maybe.None -> {
                val oldCount = itemCount
                data = null
                notifyItemRangeRemoved(0, oldCount)
            }
            is Maybe.Some -> {
                data = newData.value
                notifyDataSetChanged()
            }
        }
    }
}
