package ch.berta.fabio.popularmovies.extensions

import android.os.Parcelable
import ch.berta.fabio.popularmovies.features.base.Lifecycle
import ch.berta.fabio.popularmovies.features.base.LifecycleHandler
import ch.berta.fabio.popularmovies.features.base.StateBundle
import rx.Observable
import timber.log.Timber

fun <T> Observable<T>.debug(tag: String): Observable<T> = doOnNext { Timber.d("$tag: $it") }

fun <T> Observable<T>.bindToDestroy(lifecycleHandler: LifecycleHandler): Observable<T>
        = bindToEvent(lifecycleHandler, Lifecycle.DESTROY)

fun <T> Observable<T>.bindToStartStop(lifecycleHandler: LifecycleHandler): Observable<T>
        = lifecycleHandler.lifecycle
        .filter { it == Lifecycle.START }
        .switchMap { bindToEvent(lifecycleHandler, Lifecycle.STOP) }

fun <T> Observable<T>.bindToEvent(
        lifecycleHandler: LifecycleHandler,
        event: Lifecycle
): Observable<T> = takeUntil(lifecycleHandler.lifecycle.filter { it == event })

fun <T : Parcelable> Observable<T>.saveForConfigChange(lifecycleHandler: LifecycleHandler,
                                                       key: String,
                                                       beforeConfigChangeReducer: (T) -> T = { it }
): Observable<StateBundle<T>> = lifecycleHandler.lifecycle
        .filter { it == Lifecycle.START }
        .switchMap {
            lifecycleHandler.outStateBundle.withLatestFrom(map(beforeConfigChangeReducer),
                    { bundle, state -> StateBundle(bundle, key, state) })
                    .takeUntil(lifecycleHandler.lifecycle.filter { it == Lifecycle.STOP })
                    .doOnNext { it.bundle.putParcelable(it.key, it.state) }
        }