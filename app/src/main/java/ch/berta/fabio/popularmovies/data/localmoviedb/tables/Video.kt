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

package ch.berta.fabio.popularmovies.data.localmoviedb.tables

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import io.reactivex.Flowable

@Entity(
        tableName = "video",
        foreignKeys = arrayOf(ForeignKey(
                entity = MovieEntity::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("movie_id"),
                onDelete = CASCADE,
                onUpdate = CASCADE
        )),
        indices = arrayOf(Index("movie_id"))
)
data class VideoEntity(
        @ColumnInfo(name = "movie_id") val movieId: Int,
        val name: String,
        val key: String,
        val site: String,
        val size: Int,
        val type: String,
        @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Dao
interface VideoDao {
    @Query("SELECT id, movie_id, name, key, site, size, type FROM video WHERE movie_id = :movieId")
    fun getByMovieId(movieId: Int): Flowable<List<VideoEntity>>

    @Insert
    fun insertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM video WHERE movie_id IN (SELECT id FROM movie WHERE id = :movieId)")
    fun deleteByMovieId(movieId: Int): Int
}
