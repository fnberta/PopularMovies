package ch.berta.fabio.popularmovies.data.localmoviedb

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.*

@Database(
        entities = arrayOf(MovieEntity::class, VideoEntity::class, ReviewEntity::class),
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MovieDb : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun reviewDao(): ReviewDao
    abstract fun videoDao(): VideoDao
}


