package ch.berta.fabio.popularmovies.effects

import android.content.SharedPreferences

const val KEY_SORT_POS = "KEY_SORT_POS"

data class SharedPrefsWriteTarget(
        val key: String,
        val value: Int
)

data class SharedPrefsReadTarget(
        val key: String,
        val defaultValue: Int
)

fun persistTo(prefs: SharedPreferences, writeTarget: SharedPrefsWriteTarget) {
    prefs.edit().putInt(writeTarget.key, writeTarget.value).apply()
}

fun readFrom(prefs: SharedPreferences, readTarget: SharedPrefsReadTarget): Int {
    return prefs.getInt(readTarget.key, readTarget.defaultValue)
}
