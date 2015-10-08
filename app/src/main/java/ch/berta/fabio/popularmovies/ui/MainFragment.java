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

import ch.berta.fabio.popularmovies.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Sort;
import ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment;
import ch.berta.fabio.popularmovies.ui.adapters.MoviesRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.dialogs.SortMoviesDialogFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements
        MoviesRecyclerAdapter.AdapterInteractionListener {

    public static final String INTENT_MOVIE_SELECTED = "intent_movie_selected";
    private static final int MOVIE_DB_MAX_PAGE = 1000;
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_MOVIE_PAGE = "state_movie_page";
    private static final String STATE_REFRESHING = "state_refreshing";
    private static final String STATE_LOADING_MORE = "state_loading_more";
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

    public MainFragment() {
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
        } else {
            mMovies = new ArrayList<>();
            mIsRefreshing = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_MOVIES, mMovies);
        outState.putInt(STATE_MOVIE_PAGE, mMoviePage);
        outState.putBoolean(STATE_REFRESHING, mSwipeRefreshLayout.isRefreshing());
        outState.putBoolean(STATE_LOADING_MORE, mIsLoadingMore);
    }

    private void setupSorting() {
        mSortOptions = new Sort[]{
                new Sort(Sort.SORT_POPULARITY, getString(R.string.sort_popularity)),
                new Sort(Sort.SORT_RELEASE_DATE, getString(R.string.sort_release_date)),
                new Sort(Sort.SORT_RATING, getString(R.string.sort_rating))
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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_base);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_base);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_base);
        mViewEmpty = rootView.findViewById(R.id.empty_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSwipeToRefresh();
        setupRecyclerView();

        final int moviesSize = mMovies.size();
        if (moviesSize == 0) {
            mMoviePage = 1;
            queryMovies(true);
        } else {
            if (mIsLoadingMore) {
                // scroll to last position to show load more indicator
                mRecyclerView.scrollToPosition(moviesSize - 1);
            }

            toggleMainVisibility(false);
        }
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
                return mSwipeRefreshLayout.isRefreshing() || mIsLoadingMore;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return mMoviePage >= MOVIE_DB_MAX_PAGE;
            }
        }).start();
    }

    public void queryMovies(boolean showProgressBar) {
        FragmentManager fragmentManager = getFragmentManager();
        QueryMoviesTaskFragment task = findTaskFragment(fragmentManager);

        if (task == null) {
            if (showProgressBar) {
                toggleMainVisibility(true);
            }

            Sort sort = mSortOptions[mSortSelected];
            task = QueryMoviesTaskFragment.newInstance(mMoviePage, sort.getOption());
            fragmentManager.beginTransaction()
                    .add(task, QUERY_MOVIES_TASK)
                    .commit();
        }
    }

    private QueryMoviesTaskFragment findTaskFragment(FragmentManager fragmentManager) {
        return (QueryMoviesTaskFragment) fragmentManager.findFragmentByTag(QUERY_MOVIES_TASK);
    }

    public void onMoviesQueried(List<Movie> movies) {
        removeTaskFragment();

        if (mMoviePage == 1) {
            setRefreshing(false);
            mRecyclerAdapter.setMovies(movies);
            mRecyclerView.scrollToPosition(0);
            toggleMainVisibility(false);
        } else {
            mRecyclerAdapter.hideLoadMoreIndicator();
            mRecyclerAdapter.addMovies(movies);
            mIsLoadingMore = false;
        }

        mMoviePage++;
    }

    public void onMovieQueryFailed() {
        removeTaskFragment();
        Snackbar snackbar = Utils.getBasicSnackbar(mRecyclerView,
                getString(R.string.error_connection));

        if (mMoviePage == 1) {
            setRefreshing(false);
            toggleMainVisibility(false);

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

    private void toggleMainVisibility(boolean showProgress) {
        if (showProgress) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            toggleEmptyViewVisibility();
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

    public void onSortOptionSelected(int sortOptionIndex) {
        mMoviePage = 1;
        mSortSelected = sortOptionIndex;
        mSharedPrefs.edit().putInt(PERSIST_SORT, sortOptionIndex).apply();

        queryMovies(true);
    }
}
