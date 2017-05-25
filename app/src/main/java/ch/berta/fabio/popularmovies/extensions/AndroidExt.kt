package ch.berta.fabio.popularmovies.extensions

import android.database.Cursor
import android.provider.BaseColumns

fun Cursor.getRowId(): Long = getLongFromColumn(BaseColumns._ID)

fun Cursor.getStringFromColumn(columnName: String): String =
        getString(getColumnIndexOrThrow(columnName))

fun Cursor.getIntFromColumn(columnName: String): Int = getInt(getColumnIndexOrThrow(columnName))

fun Cursor.getLongFromColumn(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))

fun Cursor.getDoubleFromColumn(columnName: String): Double =
        getDouble(getColumnIndexOrThrow(columnName))
