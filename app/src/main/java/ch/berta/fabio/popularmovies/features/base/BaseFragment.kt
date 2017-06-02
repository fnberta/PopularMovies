package ch.berta.fabio.popularmovies.features.base

import android.arch.lifecycle.LifecycleFragment
import android.content.Context

abstract class BaseFragment<T : BaseFragment.ActivityListener> : LifecycleFragment() {

    lateinit var activityListener: T

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        @Suppress("UNCHECKED_CAST")
        activityListener = context as T
    }

    interface ActivityListener
}