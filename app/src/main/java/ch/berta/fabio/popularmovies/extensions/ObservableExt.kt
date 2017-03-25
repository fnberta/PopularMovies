package ch.berta.fabio.popularmovies.extensions

import android.os.Bundle
import android.os.Parcelable
import ch.berta.fabio.popularmovies.features.base.Lifecycle
import ch.berta.fabio.popularmovies.features.base.LifecycleHandler
import ch.berta.fabio.popularmovies.features.base.StateBundle
import rx.Observable

fun <T> Observable<T>.debug(tag: String): Observable<T> = this
        .doOnNext { timber.log.Timber.d("$tag: $it") }

fun <T> Observable<T>.bindTo(lifecycle: Observable<Lifecycle>): Observable<T> = lifecycle
        .filter { it == Lifecycle.START }
        .switchMap {
            this.takeUntil(
                    lifecycle.filter { it == Lifecycle.STOP })
        }

fun <T : Parcelable> Observable<T>.saveForConfigChange(lifecycleHandler: LifecycleHandler,
                                                       key: String,
                                                       beforeConfigChangeReducer: (T) -> T = { it }
): Observable<StateBundle<T>> = lifecycleHandler.lifecycle
        .filter { it == Lifecycle.START }
        .switchMap {
            lifecycleHandler.outStateBundle.withLatestFrom(this.map(beforeConfigChangeReducer),
                    { bundle, state -> StateBundle(bundle, key, state) })
                    .takeUntil(lifecycleHandler.lifecycle.filter { it == Lifecycle.STOP })
                    .doOnNext { it.bundle.putParcelable(it.key, it.state) }
        }