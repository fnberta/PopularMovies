package ch.berta.fabio.popularmovies

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewCompat
import android.view.View

enum class FragmentAction {
    ADD, REPLACE
}

sealed class NavigationTarget {
    data class Finish(val result: Int? = null) : NavigationTarget()
    data class Action(val action: String, val uri: Uri) : NavigationTarget()
    data class Activity(
            val name: Class<*>,
            val args: Parcelable? = null,
            val requestCode: Int = -1,
            val sharedElementView: View? = null
    ) : NavigationTarget()

    data class Fragment(
            val instance: android.support.v4.app.Fragment,
            val action: FragmentAction,
            val transition: Int = -1
    ) : NavigationTarget()
}

const val KEY_ACTIVITY_ARGS = "KEY_ACTIVITY_ARGS"

fun navigateTo(activity: FragmentActivity, target: NavigationTarget) {
    when (target) {
        is NavigationTarget.Finish -> {
            if (target.result != null) {
                activity.setResult(target.result)
            }
            ActivityCompat.finishAfterTransition(activity)
        }
        is NavigationTarget.Action -> navigateAction(activity, target)
        is NavigationTarget.Activity -> navigateActivity(activity, target)
        is NavigationTarget.Fragment -> navigateFragment(activity, target)
    }
}

fun navigateAction(activity: FragmentActivity, target: NavigationTarget.Action) {
    val intent = Intent(target.action, target.uri)
    activity.startActivity(intent)
}

fun navigateActivity(activity: FragmentActivity, target: NavigationTarget.Activity) {
    val intent = Intent(activity, target.name).apply {
        putExtra(KEY_ACTIVITY_ARGS, target.args)
    }
    val options = activity.getString(R.string.shared_transition_details_poster).let {
        ViewCompat.setTransitionName(target.sharedElementView, it)
        ActivityOptionsCompat.makeSceneTransitionAnimation(activity, target.sharedElementView, it)
    }

    if (target.requestCode > -1) {
        activity.startActivityForResult(intent, target.requestCode, options.toBundle())
    } else {
        activity.startActivity(intent, options.toBundle())
    }
}

fun navigateFragment(
        activity: FragmentActivity,
        target: NavigationTarget.Fragment,
        container: Int = R.id.container_main
) {
    activity.supportFragmentManager.beginTransaction().apply {
        when {
            target.action == FragmentAction.ADD -> add(container, target.instance)
            target.action == FragmentAction.REPLACE -> replace(container, target.instance)
        }

        if (target.transition > -1) {
            setTransition(target.transition)
        }
    }.commit()
}
