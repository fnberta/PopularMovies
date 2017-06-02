package ch.berta.fabio.popularmovies.data.localmoviedb.tables

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE

@Entity(
        tableName = "review",
        foreignKeys = arrayOf(ForeignKey(
                entity = MovieEntity::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("movie_id"),
                onDelete = CASCADE,
                onUpdate = CASCADE
        )),
        indices = arrayOf(Index("movie_id"))
)
data class ReviewEntity(
        @ColumnInfo(name = "movie_id") val movieId: Long,
        val author: String,
        val content: String,
        val url: String,
        @PrimaryKey(autoGenerate = true) val id: Long = 0
)
