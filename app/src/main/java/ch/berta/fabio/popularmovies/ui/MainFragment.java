package ch.berta.fabio.popularmovies.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_SORT_SELECTED = "state_sort_selected";
    private static final String STATE_MOVIE_PAGE = "state_movie_page";
    private static final String QUERY_MOVIES_TASK = "query_movies_task";
    private FragmentInteractionListener mListener;
    private boolean mUseTwoPane;
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

    public MainFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setupSortOptions();
        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);

        if (savedInstanceState != null) {
            mMovies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mSortSelected = savedInstanceState.getInt(STATE_SORT_SELECTED);
            mMoviePage = savedInstanceState.getInt(STATE_MOVIE_PAGE);
        } else {
            mMovies = new ArrayList<>();
            mMoviePage = 1;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_MOVIES, mMovies);
        outState.putInt(STATE_SORT_SELECTED, mSortSelected);
        outState.putInt(STATE_MOVIE_PAGE, mMoviePage);
    }

    private void setupSortOptions() {
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

        int spanCount = getResources().getInteger(R.integer.span_count);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new PosterGridItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new MoviesRecyclerAdapter(R.layout.row_movie, mMovies, this, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.red_500, R.color.red_700,
                R.color.amber_A400);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryMovies(false);
            }
        });

        if (mMovies.isEmpty()) {
            queryMovies(true);
        } else {
            toggleMainVisibility(false);
        }
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
        setLoading(false);

        mMovies.clear();
        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }

        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility(false);
    }

    public void onMovieQueryFailed() {
        removeTaskFragment();
        setLoading(false);
        toggleMainVisibility(false);

        Snackbar snackbar = Utils.getBasicSnackbar(mRecyclerView, getString(R.string.error_connection));
        snackbar.setAction(R.string.snackbar_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                queryMovies(false);
            }
        });
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

    private void setLoading(boolean isLoading) {
        mSwipeRefreshLayout.setRefreshing(isLoading);
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
        Movie movie = mMovies.get(position);

        if (!mUseTwoPane) {
            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra(INTENT_MOVIE_SELECTED, movie);
            String transitionName = getString(R.string.shared_transition_details_poster);
            ViewCompat.setTransitionName(sharedView, transitionName);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(), sharedView, transitionName);
            getActivity().startActivity(intent, options.toBundle());
        } else {
            MovieDetailsFragment detailsFragment = MovieDetailsFragment.newInstance(movie);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, detailsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    public void onSortOptionSelected(int sortOptionIndex) {
        mSortSelected = sortOptionIndex;
        queryMovies(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
    }
}
