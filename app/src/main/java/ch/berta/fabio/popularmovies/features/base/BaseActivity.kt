package ch.berta.fabio.popularmovies.features.base

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.Intent
import android.support.v7.app.AppCompatActivity

data class ActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?)

abstract class BaseActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val mRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = mRegistry
}