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

package ch.berta.fabio.popularmovies;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Provides useful static utility methods for the handling of retained headless worker fragments.
 */
public class WorkerUtils {

    private WorkerUtils() {
        // class cannot be instantiated
    }

    /**
     * Removes a worker fragment from the stack regardless of possible state loss.
     *
     * @param fragmentManager the fragment manager to use to remove the fragment
     * @param tag             the tag to find the fragment
     */
    public static void removeWorker(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment fragment = findWorker(fragmentManager, tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    /**
     * Returns the worker fragment associated with the specified tag.
     *
     * @param fragmentManager the fragment manager to use to find the fragment
     * @param tag             the tag to find the fragment
     * @return the worker fragment associated with the tag
     */
    public static Fragment findWorker(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }
}
