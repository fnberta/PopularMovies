/*
 * Copyright (c) 2017 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies

import android.animation.ObjectAnimator
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.view.View
import android.widget.TextView
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import java.text.DateFormat
import java.util.*

fun <T> Observable<T>.log(tag: String): Observable<T> = doOnNext { timber.log.Timber.d("$tag: $it") }

fun <T> Observable<T>.bindTo(lifecycleRegistry: LifecycleRegistry): Observable<T> = compose {
    it
            .takeWhile { lifecycleRegistry.currentState != Lifecycle.State.DESTROYED }
            .filter { lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED) }
}

fun <T> Observable<T>.toLiveData(
        backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST
): LiveData<T> = LiveDataReactiveStreams.fromPublisher(toFlowable(backpressureStrategy))

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

fun View.setHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

fun Date.formatLong(): String = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(this)

fun Date.formatShort(): String = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(this)
