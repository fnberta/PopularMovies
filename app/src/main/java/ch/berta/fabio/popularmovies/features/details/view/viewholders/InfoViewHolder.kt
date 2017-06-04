/*
 * Copyright (c) 2017 Fabio Berta
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

package ch.berta.fabio.popularmovies.features.details.view.viewholders

import android.support.annotation.IntegerRes
import android.widget.TextView
import ch.berta.fabio.popularmovies.databinding.RowDetailsInfoBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder
import ch.berta.fabio.popularmovies.expandOrCollapse

/**
 * Provides a [RecyclerView] row that displays basic information about a movie.
 *
 */
class InfoViewHolder(
        binding: RowDetailsInfoBinding,
        @IntegerRes maxLinesCollapsed: Int
) : BaseBindingViewHolder<RowDetailsInfoBinding>(binding) {

    init {
        binding.tvDetailsPlot.setOnClickListener { (it as TextView).expandOrCollapse(maxLinesCollapsed) }
    }
}