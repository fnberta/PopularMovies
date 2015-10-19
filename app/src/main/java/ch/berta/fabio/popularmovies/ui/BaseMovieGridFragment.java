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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.ui.adapters.listeners.MovieInteractionListener;

/**
 * Displays a grid of movie poster images.
 */
public abstract class BaseMovieGridFragment extends Fragment implements
        MovieInteractionListener {

    public static final String INTENT_MOVIE_SELECTED = "intent_movie_selected";
    public static final String INTENT_MOVIE_SELECTED_ROW_ID = "intent_movie_selected_row_id";
    public static final String FRAGMENT_TWO_PANE_DETAILS = "FRAGMENT_TWO_PANE_DETAILS";
    private static final String LOG_TAG = BaseMovieGridFragment.class.getSimpleName();
    private static final String STATE_MOVIE_SELECTED = "STATE_MOVIE_SELECTED";
    private int mMovieSelected;
    boolean mUseTwoPane;
    ProgressBar mProgressBar;
    RecyclerView mRecyclerView;
    View mViewEmpty;

    public BaseMovieGridFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        if (savedInstanceState != null && mUseTwoPane) {
            mMovieSelected = savedInstanceState.getInt(STATE_MOVIE_SELECTED);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUseTwoPane) {
            outState.putInt(STATE_MOVIE_SELECTED, mMovieSelected);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_base);
        mViewEmpty = view.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_base);
        setupRecyclerView();
    }

    protected abstract void setupRecyclerView();

    final int getLayoutWidth() {
        int screenWidth = Utils.getScreenWidth(getResources());
        return mUseTwoPane ? screenWidth / 100 *
                getResources().getInteger(R.integer.two_pane_list_width_percentage) : screenWidth;
    }

    final void toggleMainVisibility(boolean showMainGrid) {
        if (showMainGrid) {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMovieRowItemClick(int position, View sharedView) {
        Movie movie = getMovieSelected(position);
        if (movie == null) {
            // TODO: show user error message
            return;
        }

        final long rowId = getMovieRowId(position);
        if (!mUseTwoPane) {
            Activity activity = getActivity();

            Intent intent = new Intent(activity, MovieDetailsActivity.class);
            intent.putExtra(INTENT_MOVIE_SELECTED, movie);
            intent.putExtra(INTENT_MOVIE_SELECTED_ROW_ID, rowId);

            String transitionName = getString(R.string.shared_transition_details_poster);
            ViewCompat.setTransitionName(sharedView, transitionName);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, sharedView, transitionName);
            activity.startActivity(intent, options.toBundle());
        } else if (mMovieSelected != movie.getId()){
            mMovieSelected = movie.getId();

            MovieDetailsFragment detailsFragment = MovieDetailsFragment.newInstance(movie, rowId);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_details, detailsFragment, FRAGMENT_TWO_PANE_DETAILS)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    @Nullable
    protected abstract Movie getMovieSelected(int position);

    protected abstract long getMovieRowId(int position);
}
