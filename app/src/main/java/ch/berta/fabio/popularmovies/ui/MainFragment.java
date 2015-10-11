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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Sort;
import ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment;
import ch.berta.fabio.popularmovies.ui.adapters.MoviesRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.decorators.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.ui.dialogs.SortMoviesDialogFragment;

/**
 * Displays a grid of movie poster images.
 */
public class MainFragment extends Fragment implements
        MoviesRecyclerAdapter.AdapterInteractionListener {

    public static final String INTENT_MOVIE_SELECTED = "intent_movie_selected";
    private static final int MOVIE_DB_MAX_PAGE = 1000;
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_MOVIE_PAGE = "state_movie_page";
    private static final String STATE_REFRESHING = "state_refreshing";
    private static final String STATE_LOADING_MORE = "state_loading_more";
    private static final String STATE_LOADING_NEW_SORT = "state_loading_new_sort";
    private static final String QUERY_MOVIES_TASK = "query_movies_task";
    private static final String PERSIST_SORT = "persisted_sort";
    private SharedPreferences mSharedPrefs;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MoviesRecyclerAdapter mRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mViewEmpty;
    private ArrayList<Movie> mMovies;
    private Sort[] mSortOptions;
    private int mSortSelected;
    private int mMoviePage;
    private String[] mSortValues;
    private boolean mIsRefreshing;
    private boolean mIsLoadingMore;
    private boolean mIsLoadingNewSort;

    public MainFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setupSorting();

        if (savedInstanceState != null) {
            mMovies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mMoviePage = savedInstanceState.getInt(STATE_MOVIE_PAGE);
            mIsRefreshing = savedInstanceState.getBoolean(STATE_REFRESHING);
            mIsLoadingMore = savedInstanceState.getBoolean(STATE_LOADING_MORE);
            mIsLoadingNewSort = savedInstanceState.getBoolean(STATE_LOADING_NEW_SORT);
        } else {
            mMovies = new ArrayList<>();
            mIsRefreshing = false;
            mIsLoadingMore = false;
            mIsLoadingNewSort = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_MOVIES, mMovies);
        outState.putInt(STATE_MOVIE_PAGE, mMoviePage);
        outState.putBoolean(STATE_REFRESHING, mSwipeRefreshLayout.isRefreshing());
        outState.putBoolean(STATE_LOADING_MORE, mIsLoadingMore);
        outState.putBoolean(STATE_LOADING_NEW_SORT, mIsLoadingNewSort);
    }

    private void setupSorting() {
        mSortOptions = new Sort[]{
                new Sort(Sort.SORT_POPULARITY, getString(R.string.sort_popularity)),
                new Sort(Sort.SORT_RATING, getString(R.string.sort_rating)),
                new Sort(Sort.SORT_RELEASE_DATE, getString(R.string.sort_release_date))
        };
        int optionsLength = mSortOptions.length;
        mSortValues = new String[optionsLength];
        for (int i = 0; i < optionsLength; i++) {
            Sort sort = mSortOptions[i];
            mSortValues[i] = sort.getReadableValue();
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortSelected = mSharedPrefs.getInt(PERSIST_SORT, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_base);
        mViewEmpty = view.findViewById(R.id.empty_view);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_base);
        setupSwipeToRefresh();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_base);
        setupRecyclerView();

        loadMovies();
    }

    private void setupSwipeToRefresh() {
        mSwipeRefreshLayout.setColorSchemeColors(R.color.red_500, R.color.red_700,
                R.color.amber_A400);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mMoviePage = 1;
                queryMovies(false);
            }
        });

        if (mIsRefreshing) {
            // work around bug in SwipeRefreshLayout that prevents changing refresh state before it
            // is laid out, TODO: change once bug is fixed
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    setRefreshing(true);
                }
            });
        }
    }

    private void setRefreshing(boolean isRefreshing) {
        mSwipeRefreshLayout.setRefreshing(isRefreshing);
    }

    private void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mRecyclerAdapter.getItemViewType(position);
                return viewType == MoviesRecyclerAdapter.TYPE_PROGRESS ? spanCount : 1;
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new MoviesRecyclerAdapter(getActivity(), R.layout.row_movie, mMovies,
                this, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        Mugen.with(mRecyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                mIsLoadingMore = true;
                mRecyclerAdapter.showLoadMoreIndicator();
                queryMovies(false);
            }

            @Override
            public boolean isLoading() {
                return mSwipeRefreshLayout.isRefreshing() || mIsLoadingMore || mIsLoadingNewSort;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return mMoviePage >= MOVIE_DB_MAX_PAGE;
            }
        }).start();
    }

    private void loadMovies() {
        final int moviesSize = mMovies.size();
        if (moviesSize == 0) {
            mMoviePage = 1;
            queryMovies(false);
        } else {
            if (mIsLoadingMore) {
                // scroll to last position to show load more indicator
                mRecyclerView.scrollToPosition(moviesSize - 1);
            }

            if (!mIsLoadingNewSort) {
                toggleMainVisibility(true);
            }
        }
    }

    /**
     * Creates a new {@link QueryMoviesTaskFragment} if it is not being retained across a
     * configuration change to query movies from TheMovieDB.
     *
     * @param forceNewQuery whether to force a new query when there is already on going on
     */
    public void queryMovies(boolean forceNewQuery) {
        FragmentManager fragmentManager = getFragmentManager();
        QueryMoviesTaskFragment task = findTaskFragment(fragmentManager);
        Sort sort = mSortOptions[mSortSelected];

        if (task == null) {
            task = QueryMoviesTaskFragment.newInstance(mMoviePage, sort.getOption());
            fragmentManager.beginTransaction()
                    .add(task, QUERY_MOVIES_TASK)
                    .commit();
        } else if (forceNewQuery) {
            QueryMoviesTaskFragment newTask = QueryMoviesTaskFragment.newInstance(mMoviePage,
                    sort.getOption());
            fragmentManager.beginTransaction()
                    .remove(task)
                    .add(newTask, QUERY_MOVIES_TASK)
                    .commit();
        }
    }

    private QueryMoviesTaskFragment findTaskFragment(FragmentManager fragmentManager) {
        return (QueryMoviesTaskFragment) fragmentManager.findFragmentByTag(QUERY_MOVIES_TASK);
    }

    /**
     * Removes {@link QueryMoviesTaskFragment} and updates the main {@link RecyclerView} grid
     * with the queried movies.
     *
     * @param movies the list of newly queried movies containg {@link Movie} objects
     */
    public void onMoviesQueried(List<Movie> movies) {
        removeTaskFragment();

        if (mMoviePage == 1) {
            mIsLoadingNewSort = false;
            setRefreshing(false);
            mRecyclerAdapter.setMovies(movies);
            mRecyclerView.scrollToPosition(0);
            toggleMainVisibility(true);
        } else {
            mRecyclerAdapter.hideLoadMoreIndicator();
            mRecyclerAdapter.addMovies(movies);
            mIsLoadingMore = false;
        }

        mMoviePage++;
    }

    /**
     * Removes {@link QueryMoviesTaskFragment}, notifies the user that something went wrong by
     * showing a snackbar and hides loading or refreshing indicators
     */
    public void onMovieQueryFailed() {
        removeTaskFragment();
        Snackbar snackbar = Utils.getBasicSnackbar(mRecyclerView,
                getString(R.string.error_connection));

        if (mMoviePage == 1) {
            mIsLoadingNewSort = false;
            setRefreshing(false);
            toggleMainVisibility(true);

            snackbar.setAction(R.string.snackbar_retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRefreshing(true);
                    queryMovies(false);
                }
            });
        } else {
            mRecyclerAdapter.hideLoadMoreIndicator();
            mIsLoadingMore = false;
        }

        snackbar.show();
    }

    private void removeTaskFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        QueryMoviesTaskFragment task = findTaskFragment(fragmentManager);

        if (task != null) {
            fragmentManager.beginTransaction()
                    .remove(task)
                    .commitAllowingStateLoss();
        }
    }

    private void toggleMainVisibility(boolean showMainGrid) {
        if (showMainGrid) {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            toggleEmptyViewVisibility();
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private void toggleEmptyViewVisibility() {
        mViewEmpty.setVisibility(mMovies.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
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
        dialog.show(getFragmentManager(), "sort_dialog");
    }

    @Override
    public void onMovieRowItemClick(int position, View sharedView) {
        Activity activity = getActivity();

        Intent intent = new Intent(activity, MovieDetailsActivity.class);
        intent.putExtra(INTENT_MOVIE_SELECTED, mMovies.get(position));

        String transitionName = getString(R.string.shared_transition_details_poster);
        ViewCompat.setTransitionName(sharedView, transitionName);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, sharedView, transitionName);
        activity.startActivity(intent, options.toBundle());
    }

    /**
     * Sets the global sort option field to the one selected.
     *
     * @param sortOptionIndex the index number of the selected sort option
     */
    public void onSortOptionSelected(int sortOptionIndex) {
        mSortSelected = sortOptionIndex;
        mSharedPrefs.edit().putInt(PERSIST_SORT, sortOptionIndex).apply();

        toggleMainVisibility(false);
        setRefreshing(false);
        mIsLoadingMore = false;
        mIsLoadingNewSort = true;
        mMoviePage = 1;
        queryMovies(true);
    }
}
