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

package ch.berta.fabio.popularmovies.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.models.Sort;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;
import ch.berta.fabio.popularmovies.workerfragments.QueryMoviesWorker;

/**
 * Provides the main entry point to the app and hosts a {@link MovieGridFragment}.
 */
public class MovieGridActivity extends AppCompatActivity implements
        MovieGridBaseFragment.FragmentInteractionListener,
        QueryMoviesWorker.TaskInteractionListener,
        QueryMovieDetailsWorker.TaskInteractionListener,
        MovieDetailsFragment.FragmentInteractionListener {

    private static final String LOG_TAG = MovieGridActivity.class.getSimpleName();
    private static final String FRAGMENT_MOVIES = "FRAGMENT_MOVIES";
    private static final String PERSIST_SORT = "PERSIST_SORT";
    private SharedPreferences mSharedPrefs;
    private Spinner mSpinnerSort;
    private MovieGridBaseFragment mMovieGridFragment;
    private FloatingActionButton mFab;
    private boolean mUseTwoPane;
    private View mTwoPaneEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        if (mUseTwoPane) {
            mFab = (FloatingActionButton) findViewById(R.id.fab_grid_favorite);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMovieDetailsFragment().toggleFavorite();
                }
            });

            mTwoPaneEmptyView = findViewById(R.id.empty_view);
        }

        mSpinnerSort = (Spinner) findViewById(R.id.sp_grid_sort);
        setupSorting();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            final Sort sort = (Sort) mSpinnerSort.getSelectedItem();
            mMovieGridFragment = sort.getOption().equals(Sort.SORT_FAVORITE) ?
                    new MovieGridFavFragment() :
                    MovieGridFragment.newInstance(sort);

            fragmentManager.beginTransaction()
                    .add(R.id.container_main, mMovieGridFragment, FRAGMENT_MOVIES)
                    .commit();

            if (mUseTwoPane) {
                setTwoPaneEmptyViewVisibility(true);
            }
        } else {
            mMovieGridFragment = (MovieGridBaseFragment)
                    fragmentManager.getFragment(savedInstanceState, FRAGMENT_MOVIES);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, FRAGMENT_MOVIES, mMovieGridFragment);
    }

    private MovieDetailsBaseFragment getMovieDetailsFragment() {
        return (MovieDetailsBaseFragment) getSupportFragmentManager()
                .findFragmentByTag(MovieGridBaseFragment.FRAGMENT_TWO_PANE_DETAILS);
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
        mSpinnerSort.setAdapter(spinnerAdapter);
        mSpinnerSort.setSelection(sortSelected, false);
        mSpinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            if (mMovieGridFragment instanceof MovieGridFragment) {
                ((MovieGridFragment) mMovieGridFragment).onSortOptionSelected(sortSelected);
            } else {
                showMovieFragment(sortSelected);
            }
        } else if (!(mMovieGridFragment instanceof MovieGridFavFragment)) {
            showFavFragment();
        }
    }

    @Override
    public void setTwoPaneEmptyViewVisibility(boolean show) {
        if (show) {
            mTwoPaneEmptyView.setVisibility(View.VISIBLE);
        } else {
            mTwoPaneEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MovieGridBaseFragment.REQUEST_MOVIE_DETAILS &&
                resultCode == MovieDetailsFavFragment.RESULT_UNFAVOURED) {
            Snackbar.make(mSpinnerSort, getString(R.string.snackbar_removed_from_favorites),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMoviesQueried(List<Movie> movies) {
        ((MovieGridFragment) mMovieGridFragment).onMoviesQueried(movies);
    }

    @Override
    public void onMovieQueryFailed() {
        ((MovieGridFragment) mMovieGridFragment).onMovieQueryFailed();
    }

    private void showMovieFragment(Sort sortSelected) {
        mMovieGridFragment = MovieGridFragment.newInstance(sortSelected);
        replaceFragment();
    }

    private void showFavFragment() {
        mMovieGridFragment = new MovieGridFavFragment();
        replaceFragment();
    }

    private void replaceFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, mMovieGridFragment, FRAGMENT_MOVIES)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showFab() {
        mFab.show();
    }

    @Override
    public void toggleFabImage(boolean isFavoured) {
        mFab.setImageResource(isFavoured ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_outline_white_24dp);
    }

    @Override
    public void hideDetailsFragment() {
        MovieDetailsBaseFragment fragment = getMovieDetailsFragment();
        if (fragment != null) {
            mFab.hide();
            getSupportFragmentManager().beginTransaction()
                    .remove(getMovieDetailsFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }

        setTwoPaneEmptyViewVisibility(true);
    }

    @Override
    public void onMovieDetailsQueried(MovieDetails movieDetails) {
        ((MovieDetailsFragment) getMovieDetailsFragment()).onMovieDetailsQueried(movieDetails);
    }

    @Override
    public void onMovieDetailsQueryFailed() {
        ((MovieDetailsFragment) getMovieDetailsFragment()).onMovieDetailsQueryFailed();
    }

    @Override
    public void setOnePaneHeader(String title, String backdrop) {
        // do nothing, only relevant for one pane view
    }
}
