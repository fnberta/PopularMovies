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

package ch.berta.fabio.popularmovies.features.details.view

import android.database.Cursor
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.databinding.ActivityMovieDetailsBinding
import ch.berta.fabio.popularmovies.effects.ContentProviderResult
import ch.berta.fabio.popularmovies.effects.KEY_ACTIVITY_ARGS
import ch.berta.fabio.popularmovies.effects.navigateTo
import ch.berta.fabio.popularmovies.effects.persistTo
import ch.berta.fabio.popularmovies.extensions.bindToDestroy
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.details.component.*
import ch.berta.fabio.popularmovies.features.details.viewmodels.DetailsViewModel
import ch.berta.fabio.popularmovies.features.details.viewmodels.rows.DetailsVideoRowViewModel
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxrelay.BehaviorRelay
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

sealed class DetailsActivityArgs : PaperParcelable {
    @PaperParcel
    data class Onl(val movieDbId: Int) : DetailsActivityArgs() {
        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR = PaperParcelDetailsActivityArgs_Onl.CREATOR
        }
    }

    @PaperParcel
    data class Fav(val movieRowId: Long) : DetailsActivityArgs() {
        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR = PaperParcelDetailsActivityArgs_Fav.CREATOR
        }
    }
}

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar.
 */
class DetailsActivity : BaseActivity(),
                        DetailsFavActivityListener,
                        DetailsOnlActivityListener {

    override val updateSwipes: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val detailsFav: BehaviorRelay<Maybe<Cursor>> = BehaviorRelay.create()
    override val detailsOnl: BehaviorRelay<MovieDetails> = BehaviorRelay.create()
    override val detailsOnlId: BehaviorRelay<Maybe<Cursor>> = BehaviorRelay.create()
    override val videoClicks: BehaviorRelay<DetailsVideoRowViewModel> = BehaviorRelay.create()
    override lateinit var sinks: DetailsSinks
    private val viewModel = DetailsViewModel()
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMovieDetailsBinding>(this,
                R.layout.activity_movie_details)
    }
    private val contentProviderResults: BehaviorRelay<ContentProviderResult> = BehaviorRelay.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition()

        binding.viewModel = viewModel

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        sinks = setupComponent(createInitialState())
        subscribeToSinks(sinks)

        if (savedInstanceState == null) {
            addFragment()
        }
    }

    private fun addFragment() {
        val args = intent.getParcelableExtra<DetailsActivityArgs>(KEY_ACTIVITY_ARGS)
        val fragment: Fragment = when (args) {
            is DetailsActivityArgs.Fav -> DetailsFavFragment.newInstance(args.movieRowId)
            is DetailsActivityArgs.Onl -> DetailsOnlFragment.newInstance(args.movieDbId)
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment, fragment.javaClass.canonicalName)
                .commit()
    }

    private fun setupComponent(initialState: DetailsState): DetailsSinks {
        val detailViewEvents = DetailsViewEvents(
                updateSwipes,
                binding.fabDetailsFavorite.clicks(),
                videoClicks
        )
        val dataLoadEvents = DetailsDataLoadEvents(
                detailsFav,
                detailsOnl,
                detailsOnlId
        )
        val persistenceEvents = PersistenceEvents(contentProviderResults)

        val sources = DetailsSources(detailViewEvents, dataLoadEvents, persistenceEvents)
        return main(initialState, sources)
    }

    private fun subscribeToSinks(sinks: DetailsSinks) {
        sinks.state
                .bindToDestroy(lifecycleHandler)
                .subscribe { render(it) }
        sinks.contentProviderOps
                .flatMap { persistTo(contentResolver, it) }
                .bindToDestroy(lifecycleHandler)
                .subscribe(contentProviderResults)
        sinks.navigation
                .bindToDestroy(lifecycleHandler)
                .subscribe { navigateTo(this, it) }
    }

    private fun render(state: DetailsState) {
        viewModel.title = state.title
        viewModel.backdropPath = state.backdropPath
        viewModel.favoured = state.favoured
        if (state.snackbar.show) {
            Snackbar.make(binding.container, state.snackbar.message, Snackbar.LENGTH_LONG).show()
        }
    }
}
