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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Sort;
import ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment;
import ch.berta.fabio.popularmovies.ui.dialogs.SortMoviesDialogFragment;

/**
 * Provides the main entry point to the app and hosts a {@link MovieGridFragment}.
 */
public class MovieGridActivity extends AppCompatActivity implements
        QueryMoviesTaskFragment.TaskInteractionListener,
        SortMoviesDialogFragment.DialogInteractionListener,
        MovieDetailsFragment.FragmentInteractionListener {

    private static final String LOG_TAG = MovieGridActivity.class.getSimpleName();
    private static final String FRAGMENT_MOVIES = "FRAGMENT_MOVIES";
    private static final String PERSIST_SORT = "persisted_sort";
    private static final String SORT_DIALOG = "sort_dialog";
    private SharedPreferences mSharedPrefs;
    private Sort[] mSortOptions;
    private String[] mSortValues;
    private int mSortSelected;
    private BaseMovieGridFragment mMovieGridFragment;
    private FloatingActionButton mFab;
    private boolean mUseTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        if (mUseTwoPane) {
            mFab = (FloatingActionButton) findViewById(R.id.fab_grid_favorite);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MovieDetailsFragment fragment = getMovieDetailsFragment();
                    fragment.toggleFavorite();
                }
            });
        }

        setupSorting();

        if (savedInstanceState == null) {
            final Sort sort = mSortOptions[mSortSelected];
            BaseMovieGridFragment fragment = sort.getOption().equals(Sort.SORT_FAVORITE) ?
                    new FavMovieGridFragment() :
                    MovieGridFragment.newInstance(sort);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, fragment, FRAGMENT_MOVIES)
                    .commit();
        }
    }

    private MovieDetailsFragment getMovieDetailsFragment() {
        return (MovieDetailsFragment) getSupportFragmentManager()
                                .findFragmentByTag(BaseMovieGridFragment.FRAGMENT_TWO_PANE_DETAILS);
    }

    private void setupSorting() {
        mSortOptions = new Sort[]{
                new Sort(Sort.SORT_POPULARITY, getString(R.string.sort_popularity)),
                new Sort(Sort.SORT_RATING, getString(R.string.sort_rating)),
                new Sort(Sort.SORT_RELEASE_DATE, getString(R.string.sort_release_date)),
                new Sort(Sort.SORT_FAVORITE, getString(R.string.sort_favorite))
        };
        int optionsLength = mSortOptions.length;
        mSortValues = new String[optionsLength];
        for (int i = 0; i < optionsLength; i++) {
            Sort sort = mSortOptions[i];
            mSortValues[i] = sort.getReadableValue();
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSortSelected = mSharedPrefs.getInt(PERSIST_SORT, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMovieGridFragment = (BaseMovieGridFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_MOVIES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_grid_fragment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort:
                showSortDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSortDialog() {
        SortMoviesDialogFragment dialog = SortMoviesDialogFragment.newInstance(mSortValues,
                mSortSelected);
        dialog.show(getSupportFragmentManager(), SORT_DIALOG);
    }

    @Override
    public void onMoviesQueried(List<Movie> movies) {
        ((MovieGridFragment) mMovieGridFragment).onMoviesQueried(movies);
    }

    @Override
    public void onMovieQueryFailed() {
        ((MovieGridFragment) mMovieGridFragment).onMovieQueryFailed();
    }

    @Override
    public void onSortOptionSelected(int optionIndex) {
        mSortSelected = optionIndex;
        mSharedPrefs.edit().putInt(PERSIST_SORT, optionIndex).apply();

        if (mUseTwoPane) {
            hideDetailsFragment();
        }

        if (!mSortOptions[optionIndex].getOption().equals(Sort.SORT_FAVORITE)) {
            final Sort sort = mSortOptions[optionIndex];

            if (mMovieGridFragment instanceof MovieGridFragment) {
                ((MovieGridFragment) mMovieGridFragment).onSortOptionSelected(sort);
            } else {
                showMovieFragment(sort);
            }
        } else if (!(mMovieGridFragment instanceof FavMovieGridFragment)) {
            showFavFragment();
        }
    }

    @Override
    public void hideDetailsFragment() {
        MovieDetailsFragment fragment = getMovieDetailsFragment();
        if (fragment != null) {
            mFab.hide();
            getSupportFragmentManager().beginTransaction()
                    .remove(getMovieDetailsFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }
    }

    private void showMovieFragment(Sort sortSelected) {
        mMovieGridFragment = MovieGridFragment.newInstance(sortSelected);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, mMovieGridFragment, FRAGMENT_MOVIES)
                .commit();
    }

    private void showFavFragment() {
        mMovieGridFragment = new FavMovieGridFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, mMovieGridFragment, FRAGMENT_MOVIES)
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
}
