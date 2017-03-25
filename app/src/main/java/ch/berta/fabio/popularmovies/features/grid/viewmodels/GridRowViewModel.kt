/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.features.grid.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlRowViewModel

/**
 * Provides a view model for the movie row.
 */
data class GridRowViewModel(
        override val viewType: Int,
        @get:Bindable val title: String,
        @get:Bindable val releaseDate: String,
        @get:Bindable val posterPath: String?,
        @get:Bindable val posterHeight: Int
) : BaseObservable(), GridOnlRowViewModel