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

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.models.Sort;
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsBaseFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsFavFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieDetailsOnlFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieGridBaseFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieGridFavFragment;
import ch.berta.fabio.popularmovies.ui.fragments.MovieGridOnlFragment;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModel;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModelFavImpl;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModelOnlImpl;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModel;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelFav;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelFavImpl;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelOnlImpl;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;
import ch.berta.fabio.popularmovies.workerfragments.QueryMoviesWorker;

/**
 * Provides the main entry point to the app and hosts a {@link MovieGridOnlFragment}.
 */
public class MovieGridActivity extends AppCompatActivity implements
        MovieGridOnlFragment.FragmentInteractionListener,
        MovieGridFavFragment.FragmentInteractionListener,
        QueryMoviesWorker.WorkerInteractionListener,
        QueryMovieDetailsWorker.WorkerInteractionListener,
        MovieDetailsBaseFragment.FragmentInteractionListener {

    private static final String LOG_TAG = MovieGridActivity.class.getSimpleName();
    private static final String FRAGMENT_MOVIES = "FRAGMENT_MOVIES";
    private static final String FRAGMENT_TWO_PANE_DETAILS = "FRAGMENT_TWO_PANE_DETAILS";
    private static final String PERSIST_SORT = "PERSIST_SORT";
    private static final String STATE_VIEW_MODEL = "STATE_VIEW_MODEL";
    private static final String STATE_VIEW_MODEL_DETAILS = "STATE_VIEW_MODEL_DETAILS";
    private SharedPreferences mSharedPrefs;
    private boolean mUseTwoPane;
    private MovieGridViewModel mViewModel;
    private MovieDetailsViewModel mDetailsViewModel;
    private ActivityMovieGridBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_grid);

        setSupportActionBar(mBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        setupSorting();

        if (savedInstanceState == null) {
            final Sort sortSelected = (Sort) mBinding.spGridSort.getSelectedItem();

            final MovieGridBaseFragment fragment;
            if (sortSelected.getOption().equals(Sort.SORT_FAVORITE)) {
                mViewModel = new MovieGridViewModelFavImpl();
                fragment = MovieGridFavFragment.newInstance(mViewModel);
            } else {
                mViewModel = new MovieGridViewModelOnlImpl(sortSelected);
                fragment = MovieGridOnlFragment.newInstance(mViewModel);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, fragment, FRAGMENT_MOVIES)
                    .commit();
        } else {
            mViewModel = savedInstanceState.getParcelable(STATE_VIEW_MODEL);
            if (mUseTwoPane) {
                mDetailsViewModel = savedInstanceState.getParcelable(STATE_VIEW_MODEL_DETAILS);
                mBinding.setVariable(BR.viewModelDetails, mDetailsViewModel);
            }
        }

        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, mViewModel);
        if (mUseTwoPane) {
            outState.putParcelable(STATE_VIEW_MODEL_DETAILS, mDetailsViewModel);
        }
    }

    private void setupSorting() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int sortSelected = mSharedPrefs.getInt(PERSIST_SORT, 0);

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
        mBinding.spGridSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSortOptionSelected((Sort) parent.getSelectedItem(), position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onSortOptionSelected(Sort sortSelected, int position) {
        mSharedPrefs.edit().putInt(PERSIST_SORT, position).apply();

        if (mUseTwoPane) {
            hideDetailsFragment();
        }

        if (!sortSelected.getOption().equals(Sort.SORT_FAVORITE)) {
            if (mViewModel instanceof MovieGridViewModelOnl) {
                ((MovieGridViewModelOnl) mViewModel).onSortOptionSelected(sortSelected);
            } else {
                showMovieOnlFragment(sortSelected);
            }
        } else if (!(mViewModel instanceof MovieGridViewModelFav)) {
            showMovieFavFragment();
        }
    }

    private void showMovieOnlFragment(@NonNull Sort sortSelected) {
        mViewModel = new MovieGridViewModelOnlImpl(sortSelected);
        MovieGridOnlFragment fragment = MovieGridOnlFragment.newInstance(mViewModel);
        replaceFragment(fragment);
    }

    private void showMovieFavFragment() {
        mViewModel = new MovieGridViewModelFavImpl();
        MovieGridFavFragment fragment = MovieGridFavFragment.newInstance(mViewModel);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment newFragment) {
        mBinding.setViewModel(mViewModel);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, newFragment, FRAGMENT_MOVIES)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MovieGridBaseFragment.REQUEST_MOVIE_DETAILS &&
                resultCode == MovieDetailsFavFragment.RESULT_UNFAVOURED) {
            Snackbar.make(mBinding.clMain, R.string.snackbar_removed_from_favorites,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMoviesOnlineLoaded(@NonNull List<Movie> movies) {
        ((MovieGridViewModelOnl) mViewModel).onMoviesOnlineLoaded(movies);
    }

    @Override
    public void onMoviesOnlineLoadFailed() {
        ((MovieGridViewModelOnl) mViewModel).onMoviesOnlineLoadFailed();
    }

    @Override
    public void showDetailsFavFragment(long rowId) {
        mDetailsViewModel = new MovieDetailsViewModelFavImpl(rowId, true);

        final MovieDetailsFavFragment fragment = MovieDetailsFavFragment.newInstance(mDetailsViewModel);
        replaceDetailsFragment(fragment);
    }

    @Override
    public void showDetailsOnlFragment(@NonNull Movie movie) {
        mDetailsViewModel = new MovieDetailsViewModelOnlImpl(movie, true);

        final MovieDetailsOnlFragment fragment = MovieDetailsOnlFragment.newInstance(mDetailsViewModel);
        replaceDetailsFragment(fragment);
    }

    private void replaceDetailsFragment(@NonNull MovieDetailsBaseFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_details, fragment, FRAGMENT_TWO_PANE_DETAILS)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        mViewModel.setUserSelectedMovie(true);
        mBinding.setVariable(BR.viewModelDetails, mDetailsViewModel);
    }

    @Override
    public void hideDetailsFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment detailsFragment = fragmentManager.findFragmentByTag(FRAGMENT_TWO_PANE_DETAILS);
        fragmentManager.beginTransaction()
                .remove(detailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();

        mViewModel.setUserSelectedMovie(false);
    }

    @Override
    public void onMovieDetailsOnlineLoaded(@NonNull MovieDetails movieDetails) {
        mDetailsViewModel.onMovieDetailsOnlineLoaded(movieDetails);
    }

    @Override
    public void onMovieDetailsOnlineLoadFailed() {
        mDetailsViewModel.onMovieDetailsOnlineLoadFailed();
    }
}
