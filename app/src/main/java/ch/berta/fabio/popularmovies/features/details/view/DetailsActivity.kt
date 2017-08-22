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

package ch.berta.fabio.popularmovies.features.details.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import ch.berta.fabio.popularmovies.PopularMovies
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.databinding.ActivityMovieDetailsBinding
import ch.berta.fabio.popularmovies.di.ApplicationComponent
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.vdos.DetailsHeaderViewData
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModelFactory
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModelOnePane
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.features.grid.viewmodel.MoviesState
import javax.inject.Inject

const val KEY_EXTRAS = "KEY_EXTRAS"
const val RQ_DETAILS = 1
const val RS_DELETED_FROM_FAV = 3

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar.
 */
class DetailsActivity : BaseActivity(), BaseFragment.ActivityListener {

    @Inject
    lateinit var movieStorage: MovieStorage
    private val viewModel by lazy {
        val factory = DetailsViewModelFactory(movieStorage)
        ViewModelProviders.of(this, factory).get(DetailsViewModelOnePane::class.java)
    }
    private val component: ApplicationComponent by lazy { PopularMovies.getAppComponent(this) }
    private val viewData = DetailsHeaderViewData()
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMovieDetailsBinding>(this, R.layout.activity_movie_details)
    }

    companion object {
        fun startWithExtras(activity: FragmentActivity, extras: SelectedMovie, rqCode: Int, bundle: Bundle?) {
            val intent = Intent(activity, DetailsActivity::class.java).apply {
                putExtra(KEY_EXTRAS, extras)
            }
            activity.startActivityForResult(intent, rqCode, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition()

        component.inject(this)
        binding.viewData = viewData

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        initViewModel()
        binding.fabDetailsFavorite.setOnClickListener { viewModel.favClicks.accept(Unit) }

        if (savedInstanceState == null) {
            addFragment()
            sendSelectedMovie()
        }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer<MoviesState> {
            if (it is MoviesState.Details) {
                render(it.value)
            }
        })
    }

    private fun render(state: DetailsState) {
        viewData.title = state.title
        viewData.backdrop = state.backdrop
        viewData.favoured = state.favoured
    }

    private fun addFragment() {
        val fragment = DetailsFragment()
        supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment, fragment.javaClass.canonicalName)
                .commit()
    }

    private fun sendSelectedMovie() {
        val selectedMovie = intent.getParcelableExtra<SelectedMovie>(KEY_EXTRAS)
        viewModel.movieSelections.accept(selectedMovie)
    }
}
