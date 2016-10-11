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

package ch.berta.fabio.popularmovies.presentation.grid;

import android.content.ContentProviderResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.widget.ArrayAdapter;

import java.util.List;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.common.BaseActivity;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsBaseFragment;
import ch.berta.fabio.popularmovies.presentation.details.fav.MovieDetailsFavFragment;
import ch.berta.fabio.popularmovies.presentation.grid.fav.MovieGridFavFragment;
import ch.berta.fabio.popularmovies.presentation.grid.onl.MovieGridOnlFragment;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsViewModel;
import ch.berta.fabio.popularmovies.presentation.details.fav.MovieDetailsViewModelFav;
import ch.berta.fabio.popularmovies.presentation.details.onl.MovieDetailsViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.grid.onl.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMovieDetailsWorkerListener;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMoviesWorkerListener;
import ch.berta.fabio.popularmovies.presentation.workerfragments.UpdateMovieDetailsWorkerListener;
import rx.Observable;

/**
 * Provides the main entry point to the app and hosts a {@link MovieGridOnlFragment}.
 */
public class MovieGridActivity extends BaseActivity<MovieGridViewModel>
        implements MovieGridBaseFragment.ActivityListener,
        MovieDetailsBaseFragment.ActivityListener,
        QueryMoviesWorkerListener,
        QueryMovieDetailsWorkerListener,
        UpdateMovieDetailsWorkerListener {

    public static final String FRAGMENT_MOVIES = "FRAGMENT_MOVIES";
    public static final String FRAGMENT_TWO_PANE_DETAILS = "FRAGMENT_TWO_PANE_DETAILS";
    public static final String PERSIST_SORT = "PERSIST_SORT";
    private static final String LOG_TAG = MovieGridActivity.class.getSimpleName();
    @Inject
    SharedPreferences mSharedPrefs;
    private MovieDetailsViewModel mDetailsViewModel;
    private ActivityMovieGridBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_grid);
        PopularMovies.getAppComponent(this).inject(this);

        setSupportActionBar(mBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        setupSorting();

        if (savedInstanceState == null) {
            addFragment();
        }
    }

    private void setupSorting() {
        final int sortSelected = mSharedPrefs.getInt(PERSIST_SORT, 0);
        Sort[] sortOptions = new Sort[]{
                new Sort(Sort.SORT_POPULARITY, getString(R.string.sort_popularity)),
                new Sort(Sort.SORT_RATING, getString(R.string.sort_rating)),
                new Sort(Sort.SORT_RELEASE_DATE, getString(R.string.sort_release_date)),
                new Sort(Sort.SORT_FAVORITE, getString(R.string.sort_favorite))
        };

        ArrayAdapter<Sort> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_toolbar, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spGridSort.setAdapter(spinnerAdapter);
        mBinding.spGridSort.setSelection(sortSelected, false);
    }

    private void addFragment() {
        final Sort sortSelected = (Sort) mBinding.spGridSort.getSelectedItem();
        final MovieGridBaseFragment fragment = sortSelected.getOption().equals(Sort.SORT_FAVORITE)
                ? new MovieGridFavFragment()
                : MovieGridOnlFragment.newInstance(sortSelected);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container_main, fragment, FRAGMENT_MOVIES)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MovieGridBaseFragment.REQUEST_MOVIE_DETAILS
                && resultCode == MovieDetailsFavFragment.RESULT_UNFAVOURED) {
            Snackbar.make(mBinding.clMain, R.string.snackbar_movie_removed_from_favorites,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setViewModel(@NonNull MovieGridViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    public void setViewModel(@NonNull MovieDetailsViewModel viewModel) {
        mDetailsViewModel = viewModel;
        mBinding.setViewModelDetails(viewModel);
    }

    @Override
    public void hideDetailsFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment detailsFragment = fragmentManager.findFragmentByTag(FRAGMENT_TWO_PANE_DETAILS);
        if (detailsFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(detailsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }

        mViewModel.setUserSelectedMovie(false);
    }

    @Override
    public void setQueryMovieDetailsStream(@NonNull Observable<MovieDetails> observable, @NonNull String workerTag) {
        ((MovieDetailsViewModelOnl) mViewModel).setQueryMovieDetailsStream(observable, workerTag);
    }

    @Override
    public void setQueryMoviesStream(@NonNull Observable<List<Movie>> observable, @NonNull String workerTag) {
        ((MovieGridViewModelOnl) mViewModel).setQueryMoviesStream(observable, workerTag);
    }

    @Override
    public void setUpdateMovieDetailsStream(@NonNull Observable<ContentProviderResult[]> observable, @NonNull String workerTag) {
        ((MovieDetailsViewModelFav) mDetailsViewModel).setUpdateMovieDetailsStream(observable, workerTag);
    }
}
