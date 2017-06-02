package ch.berta.fabio.popularmovies.data

import android.content.SharedPreferences
import io.reactivex.Observable
import javax.inject.Inject

const val KEY_SORT_POS = "KEY_SORT_POS"

class SharedPrefs @Inject constructor(val sharedPreferences: SharedPreferences) {

    fun sortPos(): Observable<Int> = Observable.fromCallable { sharedPreferences.getInt(KEY_SORT_POS, 0) }

    fun writeSortPos(sortPos: Int): Observable<Unit> =
            Observable.fromCallable { sharedPreferences.edit().putInt(KEY_SORT_POS, sortPos).apply() }
}
