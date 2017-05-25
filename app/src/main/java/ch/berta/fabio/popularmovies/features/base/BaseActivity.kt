package ch.berta.fabio.popularmovies.features.base

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxrelay.BehaviorRelay
import rx.Observable

data class FrameworkEvents(val activityResult: Observable<ActivityResult>)

data class ActivityResult(val requestCode: Int, val resultCode: Int, val intent: Intent?)

abstract class BaseActivity : AppCompatActivity() {

    val lifecycleHandler: LifecycleHandler = LifecycleHandler()
    private val activityResult: BehaviorRelay<ActivityResult> = BehaviorRelay.create()
    val frameworkEvents = FrameworkEvents(activityResult)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        activityResult.call(ActivityResult(requestCode, resultCode, data))
    }
}