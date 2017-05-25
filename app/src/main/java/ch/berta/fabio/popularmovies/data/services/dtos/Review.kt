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

package ch.berta.fabio.popularmovies.data.services.dtos

import android.content.ContentValues
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Review

/**
 * Represents a review of a movie, obtained from TheMovieDb.
 */
data class Review(
        val author: String,
        val content: String,
        val url: String
) {

    override fun toString(): String {
        return author + ": " + content
    }

    fun getContentValuesEntry(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(Review.COLUMN_AUTHOR, author)
        contentValues.put(Review.COLUMN_CONTENT, content)
        contentValues.put(Review.COLUMN_URL, url)
        return contentValues
    }
}
