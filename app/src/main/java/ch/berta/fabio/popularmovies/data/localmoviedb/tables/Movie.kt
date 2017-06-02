package ch.berta.fabio.popularmovies.data.localmoviedb.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "movie")
open class MovieEntity(
        @PrimaryKey val id: Int,
        val title: String,
        @ColumnInfo(name = "release_date") val releaseDate: Date,
        @ColumnInfo(name = "vote_average") val voteAverage: Double,
        val overview: String,
        val poster: String,
        val backdrop: String
)
