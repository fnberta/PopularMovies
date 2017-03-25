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
import ch.berta.fabio.popularmovies.data.storage.MovieContract.Movie
import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

/**
 * Represents a movie, queried from TheMovieDB.
 */
@PaperParcel
data class Movie(
        @SerializedName("backdrop_path") val backdropPath: String,
        @SerializedName("id") val dbId: Int,
        val overview: String,
        @SerializedName("release_date") val releaseDate: Date,
        @SerializedName("poster_path") val posterPath: String,
        val title: String,
        @SerializedName("vote_average") val voteAverage: Double,
        val favoured: Boolean,
        val reviews: List<Review>,
        val videos: List<Video>,
        val reviewsAndVideosSet: Boolean = true
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelMovie.CREATOR
    }

    /**
     * Returns a {@link ContentValues} object with the movie's data.
     *
     * @return a {@link ContentValues} object with the movie's data
     */
    fun getContentValuesEntry(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(Movie.COLUMN_DB_ID, dbId)
        contentValues.put(Movie.COLUMN_TITLE, title)
        contentValues.put(Movie.COLUMN_RELEASE_DATE, releaseDate.time)
        contentValues.put(Movie.COLUMN_VOTE_AVERAGE, voteAverage)
        contentValues.put(Movie.COLUMN_PLOT, overview)
        contentValues.put(Movie.COLUMN_POSTER, posterPath)
        contentValues.put(Movie.COLUMN_BACKDROP, backdropPath)
        return contentValues
    }
}
