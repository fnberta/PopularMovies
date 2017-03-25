package ch.berta.fabio.popularmovies.features.base

import android.os.Bundle
import com.jakewharton.rxrelay.BehaviorRelay

enum class Lifecycle {
    CREATE, ENTER, START, STOP, DESTROY
}

data class StateBundle<out T>(val bundle: Bundle, val key: String, val state: T)

class LifecycleHandler {
    val lifecycle: BehaviorRelay<Lifecycle> = BehaviorRelay.create()
    val outStateBundle: BehaviorRelay<Bundle> = BehaviorRelay.create()

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycle.call(Lifecycle.CREATE)
        } else {
            lifecycle.call(Lifecycle.ENTER)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        if (outState != null) {
            outStateBundle.call(outState)
        }
    }

    fun onStart() {
        lifecycle.call(Lifecycle.START)
    }

    fun onStop() {
        lifecycle.call(Lifecycle.STOP)
    }

    fun onDestroy() {
        lifecycle.call(Lifecycle.DESTROY)
    }
}
