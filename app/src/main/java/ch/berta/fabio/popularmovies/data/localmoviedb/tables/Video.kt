package ch.berta.fabio.popularmovies.data.localmoviedb.tables

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE

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
