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

package ch.berta.fabio.popularmovies.features.details

import android.databinding.DataBindingUtil
import android.os.Bundle
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.databinding.ActivityMovieDetailsBinding
import ch.berta.fabio.popularmovies.features.base.BaseActivity

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar.
 */
class DetailsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition()

        val binding = DataBindingUtil.setContentView<ActivityMovieDetailsBinding>(this,
                R.layout.activity_movie_details)

        setSupportActionBar(binding!!.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

//        if (savedInstanceState == null) {
//            val rowId = intent.getLongExtra(
//                    MovieGridFavFragment.INTENT_MOVIE_SELECTED_ROW_ID, RecyclerView.NO_ID)
//            val fragment: MovieDetailsBaseFragment<*>
//            if (rowId != RecyclerView.NO_ID) {
//                fragment = MovieDetailsFavFragment.newInstance(rowId)
//            } else {
//                val movie = intent.getParcelableExtra<Movie>(
//                        MovieGridOnlFragment.INTENT_MOVIE_SELECTED)
//                fragment = MovieDetailsOnlFragment.newInstance(movie)
//            }
//
//            supportFragmentManager.beginTransaction()
//                    .add(R.id.container, fragment, DETAILS_FRAGMENT)
//                    .commit()
//        }
    }
}
