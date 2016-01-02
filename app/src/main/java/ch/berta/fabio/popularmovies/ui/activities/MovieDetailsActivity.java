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

package ch.berta.fabio.popularmovies.ui.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.databinding.ActivityMovieDetailsBinding;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsBaseFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsFavFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsOnlFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieGridFavFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieGridOnlFragment;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModel;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModelFavImpl;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModelOnlImpl;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar and hosts a
 * {@link MovieDetailsOnlFragment} that displays other information about the movie.
 */
public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsOnlFragment.FragmentInteractionListener,
        QueryMovieDetailsWorker.WorkerInteractionListener {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final String DETAILS_FRAGMENT = "details_fragment";
    private static final String STATE_VIEW_MODEL = "STATE_VIEW_MODEL";
    private MovieDetailsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition();

        final ActivityMovieDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(null);
        }

        if (savedInstanceState == null) {
            final MovieDetailsBaseFragment fragment;
            final long rowId = getIntent().getLongExtra(
                    MovieGridFavFragment.INTENT_MOVIE_SELECTED_ROW_ID, RecyclerView.NO_ID);
            if (rowId != RecyclerView.NO_ID) {
                mViewModel = new MovieDetailsViewModelFavImpl(rowId, false);
                fragment = MovieDetailsFavFragment.newInstance(mViewModel);
            } else {
                final Movie movie = getIntent().getParcelableExtra(MovieGridOnlFragment.INTENT_MOVIE_SELECTED);
                mViewModel = new MovieDetailsViewModelOnlImpl(movie, false);
                fragment = MovieDetailsOnlFragment.newInstance(mViewModel);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, DETAILS_FRAGMENT)
                    .commit();
        } else {
            mViewModel = savedInstanceState.getParcelable(STATE_VIEW_MODEL);
        }

        binding.setViewModel(mViewModel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, mViewModel);
    }

    @Override
    public void onMovieDetailsOnlineLoaded(@NonNull MovieDetails movieDetails) {
        mViewModel.onMovieDetailsOnlineLoaded(movieDetails);
    }

    @Override
    public void onMovieDetailsOnlineLoadFailed() {
        mViewModel.onMovieDetailsOnlineLoadFailed();
    }

    @Override
    public void hideDetailsFragment() {
        // do nothing, only relevant for two pane view
    }
}
