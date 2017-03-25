package ch.berta.fabio.popularmovies.data

import android.content.Context
import android.support.v4.content.Loader
import rx.Observable
import rx.Subscription
import rx.subjects.ReplaySubject

/**
 * Provides an abstract base class for a loader that returns a RxJava [Observable] as a
 * result.
 */
abstract class RxLoader<T>(context: Context) : Loader<Observable<T>>(context) {

    private val subject: ReplaySubject<T> = ReplaySubject.create<T>()
    private var subscription: Subscription? = null

    override fun onStartLoading() {
        super.onStartLoading()

        if (takeContentChanged() || !subject.hasValue()) {
            forceLoad()
        } else {
            deliverResult(subject.asObservable())
        }
    }

    override fun onForceLoad() {
        super.onForceLoad()

        subscription = getObservable().subscribe(subject)
        deliverResult(subject.asObservable())
    }

    abstract fun getObservable(): Observable<T>

    override fun deliverResult(observable: Observable<T>) {
        if (isStarted) {
            super.deliverResult(observable)
        }
    }

    override fun onCancelLoad(): Boolean {
        subscription?.unsubscribe()
        return true
    }

    override fun onStopLoading() {
        super.onStopLoading()

        subscription?.unsubscribe()
    }

    override fun onReset() {
        super.onReset()

        onStopLoading()
    }
}
