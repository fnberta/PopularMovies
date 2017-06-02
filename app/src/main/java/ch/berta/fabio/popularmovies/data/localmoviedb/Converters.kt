package ch.berta.fabio.popularmovies.data.localmoviedb

import android.arch.persistence.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date = Date(value ?: 0L)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long = date?.time ?: 0L
}