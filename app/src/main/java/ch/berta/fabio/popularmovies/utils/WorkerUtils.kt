/*
 * Copyright (c) 2015 Fabio Berta
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

package ch.berta.fabio.popularmovies.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

/**
 * Removes a worker fragment from the stack regardless of possible state loss.

 * @param fragmentManager the fragment manager to use to remove the fragment
 * *
 * @param tag             the name to find the fragment
 */
fun removeWorker(fragmentManager: FragmentManager, tag: String) {
    val fragment = findWorker(fragmentManager, tag)

    if (fragment != null) {
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
}

/**
 * Returns the worker fragment associated with the specified name.

 * @param fragmentManager the fragment manager to use to find the fragment
 * *
 * @param tag             the name to find the fragment
 * *
 * @return the worker fragment associated with the name
 */
fun findWorker(fragmentManager: FragmentManager, tag: String): Fragment? {
    return fragmentManager.findFragmentByTag(tag)
}
