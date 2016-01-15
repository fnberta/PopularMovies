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

package ch.berta.fabio.popularmovies.presentation.ui.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import ch.berta.fabio.popularmovies.databinding.ActivityMovieDetailsBinding;
import ch.berta.fabio.popularmovies.presentation.ui.fragments.MovieDetailsBaseFragment;
import ch.berta.fabio.popularmovies.presentation.ui.fragments.MovieDetailsFavFragment;
import ch.berta.fabio.popularmovies.presentation.ui.fragments.MovieDetailsOnlFragment;
import ch.berta.fabio.popularmovies.presentation.ui.fragments.MovieGridFavFragment;
import ch.berta.fabio.popularmovies.presentation.ui.fragments.MovieGridOnlFragment;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModel;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelFav;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMovieDetailsWorkerListener;
import ch.berta.fabio.popularmovies.presentation.workerfragments.UpdateMovieDetailsWorkerListener;

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar and hosts a
 * {@link MovieDetailsOnlFragment} that displays other information about the movie.
 */
public class MovieDetailsActivity extends BaseActivity<MovieDetailsViewModel> implements
        MovieDetailsBaseFragment.ActivityListener,
        QueryMovieDetailsWorkerListener, UpdateMovieDetailsWorkerListener {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final String DETAILS_FRAGMENT = "details_fragment";
    private ActivityMovieDetailsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(null);
        }

        if (savedInstanceState == null) {
            final long rowId = getIntent().getLongExtra(
                    MovieGridFavFragment.INTENT_MOVIE_SELECTED_ROW_ID, RecyclerView.NO_ID);
            final MovieDetailsBaseFragment fragment;
            if (rowId != RecyclerView.NO_ID) {
                fragment = MovieDetailsFavFragment.newInstance(rowId);
            } else {
                final Movie movie = getIntent().getParcelableExtra(MovieGridOnlFragment.INTENT_MOVIE_SELECTED);
                fragment = MovieDetailsOnlFragment.newInstance(movie);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, DETAILS_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public void setViewModel(@NonNull MovieDetailsViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    public void onMovieDetailsOnlineLoaded(@NonNull MovieDetails movieDetails) {
        ((MovieDetailsViewModelOnl) mViewModel).onMovieDetailsOnlineLoaded(movieDetails);
    }

    @Override
    public void onMovieDetailsOnlineLoadFailed() {
        ((MovieDetailsViewModelOnl) mViewModel).onMovieDetailsOnlineLoadFailed();
    }

    @Override
    public void onMovieDetailsUpdated() {
        ((MovieDetailsViewModelFav) mViewModel).onMovieDetailsUpdated();
    }

    @Override
    public void onMovieDetailsUpdateFailed() {
        ((MovieDetailsViewModelFav) mViewModel).onMovieDetailsUpdateFailed();
    }

    @Override
    public void hideDetailsFragment() {
        // do nothing, only relevant for two pane view
    }
}
