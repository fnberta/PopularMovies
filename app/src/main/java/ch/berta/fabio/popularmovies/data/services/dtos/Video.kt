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
import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.data.storage.MovieContract

const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="
const val YOU_TUBE = "YouTube"

/**
 * Represents a video (e.g. trailer) of a movie, obtained from TheMovieDb.
 */
data class Video(
        @get:Bindable val name: String,
        @get:Bindable val key: String,
        @get:Bindable val site: String,
        @get:Bindable val size: Int,
        val type: String
) : BaseObservable() {

    fun siteIsYouTube(): Boolean {
        return site == YOU_TUBE
    }

    fun getContentValuesEntry(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(MovieContract.Video.COLUMN_NAME, name)
        contentValues.put(MovieContract.Video.COLUMN_KEY, key)
        contentValues.put(MovieContract.Video.COLUMN_SITE, site)
        contentValues.put(MovieContract.Video.COLUMN_SIZE, size)
        contentValues.put(MovieContract.Video.COLUMN_TYPE, type)
        return contentValues
    }
}
