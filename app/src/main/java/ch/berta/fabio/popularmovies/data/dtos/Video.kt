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

package ch.berta.fabio.popularmovies.data.dtos

const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="
const val YOU_TUBE = "YouTube"

/**
 * Represents a video (e.g. trailer) of a movie, obtained from TheMovieDb.
 */
data class Video(
        val name: String,
        val key: String,
        val site: String,
        val size: Int,
        val type: String
) {
    fun siteIsYouTube(): Boolean = site == ch.berta.fabio.popularmovies.data.dtos.YOU_TUBE
}
