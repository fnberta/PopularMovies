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

import java.util.*

data class MovieDetails(
        val isFav: Boolean,
        val id: Int,
        val title: String,
        val overview: String,
        val releaseDate: Date,
        val voteAverage: Double,
        val poster: String,
        val backdrop: String,
        val videos: List<Video>,
        val reviews: List<Review>
)