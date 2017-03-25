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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.features.common.rows.ProgressRow
import ch.berta.fabio.popularmovies.features.grid.rows.BindingMovieRow
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridRowViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlRowViewModel
import com.jakewharton.rxrelay.BehaviorRelay

/**
 * Provides the adapter for a movie poster images grid.
 */
class GridOnlRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val movies = mutableListOf<GridOnlRowViewModel>()
    val movieClicks: BehaviorRelay<Int> = BehaviorRelay.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            R.layout.row_movie -> {
                val binding = RowMovieBinding.inflate(inflater, parent, false)
                return BindingMovieRow(binding, movieClicks)
            }
            R.layout.row_progress -> {
                val view = inflater.inflate(R.layout.row_progress, parent, false)
                return ProgressRow(view)
            }
            else -> throw Throwable("there is no type that matches the type $viewType, " +
                    "make sure your using types correctly")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == R.layout.row_movie) {
            val movieRow = holder as BindingMovieRow
            movieRow.binding.viewModel = movies[position] as GridRowViewModel
            movieRow.binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return movies[position].viewType
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    fun swapData(newMovies: List<GridOnlRowViewModel>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }
}
