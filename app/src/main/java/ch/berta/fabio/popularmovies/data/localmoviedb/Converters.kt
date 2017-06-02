package ch.berta.fabio.popularmovies.data.localmoviedb

import java.util.*

class Converters {
    @android.arch.persistence.room.TypeConverter
    fun fromTimestamp(value: Long?): Date = Date(value ?: 0L)

    @android.arch.persistence.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long = date?.time ?: 0L
}