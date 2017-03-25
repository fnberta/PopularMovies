package ch.berta.fabio.popularmovies.features.base

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment

abstract class BaseFragment<T : BaseFragment.ActivityListener> : Fragment() {

    lateinit var activityListener: T

    val lifecycleHandler: LifecycleHandler = LifecycleHandler()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        @Suppress("UNCHECKED_CAST")
        activityListener = context as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleHandler.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        lifecycleHandler.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        lifecycleHandler.onStart()
    }

    override fun onStop() {
        super.onStop()

        lifecycleHandler.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleHandler.onDestroy()
    }

    interface ActivityListener
}