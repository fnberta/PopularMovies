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

package ch.berta.fabio.popularmovies.features.grid

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

enum class SortOption {
    SORT_POPULARITY, SORT_RATING, SORT_RELEASE_DATE, SORT_FAVORITE
}

/**
 * Represents an value how to sort a movie poster images list or grid.
 */
@PaperParcel
data class Sort(
        val option: SortOption,
        val value: String,
        val readableValue: String
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelSort.CREATOR
    }

    override fun toString(): String {
        return readableValue
    }
}

data class SortSelectionState(
        val sort: Sort,
        val sortPrev: Sort
)
