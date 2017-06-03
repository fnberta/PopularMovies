/*
 * Copyright (c) 2017 Fabio Berta
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