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

import ch.berta.fabio.popularmovies.R
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

enum class SortOption(val value: String) {
    SORT_POPULARITY("popularity.desc"),
    SORT_RATING("vote_average.desc"),
    SORT_RELEASE_DATE("release_date.desc"),
    SORT_FAVORITE("favorite")
}

/**
 * Represents an value how to sort a movie poster images list or grid.
 */
@PaperParcel
data class Sort(val option: SortOption, val title: String) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelSort.CREATOR
    }

    override fun toString(): String = title
}

data class SortSelectionState(val sort: Sort, val sortPrev: Sort)

fun makeSortOptions(getTitle: (Int) -> String): List<Sort> = listOf(
        Sort(SortOption.SORT_POPULARITY, getTitle(R.string.sort_popularity)),
        Sort(SortOption.SORT_RATING, getTitle(R.string.sort_rating)),
        Sort(SortOption.SORT_RELEASE_DATE, getTitle(R.string.sort_release_date)),
        Sort(SortOption.SORT_FAVORITE, getTitle(R.string.sort_favorite))
)
