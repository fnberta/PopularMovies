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

package ch.berta.fabio.popularmovies.features.grid

import android.provider.BaseColumns
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingRow
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridRowViewModel
import ch.berta.fabio.popularmovies.features.grid.rows.BindingMovieRow
import ch.berta.fabio.popularmovies.utils.calcPosterHeight
import com.jakewharton.rxrelay.BehaviorRelay
import java.text.DateFormat
import java.util.*

/**
 * Provides the adapter for a movie poster images grid.
 */
class GridFavRecyclerAdapter(
        private var data: Sequence<Map<String, Any?>>,
        private val posterHeight: Int
) : RecyclerView.Adapter<BindingMovieRow>() {

    val movieClicks: BehaviorRelay<Int> = BehaviorRelay.create()
    private val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingMovieRow {
        val binding = RowMovieBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return BindingMovieRow(binding, movieClicks)
    }

    override fun onBindViewHolder(holder: BindingMovieRow, position: Int) {
        val item = data.elementAt(position)
        val title = item[MovieContract.Movie.COLUMN_TITLE] as String
        val date = Date(item[MovieContract.Movie.COLUMN_RELEASE_DATE] as Long)
        val dateFormatted = dateFormatter.format(date)
        val poster = item[MovieContract.Movie.COLUMN_POSTER] as String

        holder.binding.viewModel = GridRowViewModel(R.layout.row_movie, title, dateFormatted,
                poster, posterHeight)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    override fun getItemId(position: Int): Long {
        val item = data.elementAt(position)
        return item[BaseColumns._ID] as? Long ?: RecyclerView.NO_ID
    }

    /**
     * Returns the TheMovieDB id for the movie at position.

     * @param position the position of the movie
     * *
     * @return the TheMovieDB id
     */
    fun getMovieDbIdForPosition(position: Int): Int {
        val item = data.elementAt(position)
        return item[MovieContract.Movie.COLUMN_DB_ID] as Int
    }

    fun swapData(newData: Sequence<Map<String, Any?>>) {
        if (newData == data) {
            return
        }

        val itemCount = this.itemCount
        data = newData
        if (this.itemCount > 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, itemCount)
        }
    }
}
