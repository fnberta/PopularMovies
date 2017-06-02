package ch.berta.fabio.popularmovies

import android.animation.ObjectAnimator
import android.widget.TextView
import io.reactivex.Observable
import java.text.DateFormat
import java.util.*


fun <T> Observable<T>.log(tag: String): Observable<T> = doOnNext { timber.log.Timber.d("$tag: $it") }

private const val COLLAPSE_EXPAND_ANIM_TIME: Long = 300
private const val MAX_LINES_EXPANDED = 500

/**
 * Expands or collapses a [TextView] by increasing or decreasing its maxLines setting.
 *
 * @param maxLinesCollapsed the number of lines in the collapsed state
 */
fun TextView.expandOrCollapse(maxLinesCollapsed: Int) {
    val value = if (maxLines == maxLinesCollapsed) MAX_LINES_EXPANDED else maxLinesCollapsed
    val anim = ObjectAnimator.ofInt(this, "maxLines", value)
    anim.duration = COLLAPSE_EXPAND_ANIM_TIME
    anim.start()
}

fun Date.formatLong(): String = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(this)

fun Date.formatShort(): String = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(this)
