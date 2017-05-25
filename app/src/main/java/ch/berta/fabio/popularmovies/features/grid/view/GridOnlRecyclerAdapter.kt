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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding
import ch.berta.fabio.popularmovies.features.common.viewholders.DefaultViewHolder
import ch.berta.fabio.popularmovies.features.grid.component.SelectedMovie
import ch.berta.fabio.popularmovies.features.grid.view.viewholders.MovieViewHolder
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridOnlRowViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodels.rows.GridRowViewModel
import com.jakewharton.rxrelay.BehaviorRelay

/**
 * Provides the adapter for a movie poster images grid.
 */
class GridOnlRecyclerAdapter(
        val movieClicks: BehaviorRelay<SelectedMovie>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val movies = mutableListOf<GridOnlRowViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.row_movie -> RowMovieBinding.inflate(inflater, parent, false).let {
                MovieViewHolder(it).apply {
                    clicks
                            .map {
                                val viewModel = movies[it.position] as GridRowViewModel
                                SelectedMovie.Onl(viewModel.dbId, it.posterView)
                            }
                            .subscribe(movieClicks)
                }
            }
            R.layout.row_progress -> inflater.inflate(R.layout.row_progress, parent, false).let {
                DefaultViewHolder(it)
            }
            else -> throw RuntimeException("there is no type that matches the type $viewType, " +
                    "make sure you are using types correctly")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == R.layout.row_movie) {
            val movieRow = holder as MovieViewHolder
            movieRow.binding.viewModel = movies[position] as GridRowViewModel
            movieRow.binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int = movies[position].viewType

    override fun getItemCount(): Int = movies.size

    fun swapData(newMovies: List<GridOnlRowViewModel>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }
}
