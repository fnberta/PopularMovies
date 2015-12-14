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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.ui.adapters.listeners.MovieInteractionListener;

/**
 * Provides an abstract base class for the display of movies in a grid of posters.
 */
public abstract class MovieGridBaseFragment extends Fragment implements
        MovieInteractionListener {

    public static final String FRAGMENT_TWO_PANE_DETAILS = "FRAGMENT_TWO_PANE_DETAILS";
    public static final int REQUEST_MOVIE_DETAILS = 1;
    private static final String LOG_TAG = MovieGridBaseFragment.class.getSimpleName();
    private static final String STATE_MOVIE_SELECTED = "STATE_MOVIE_SELECTED";
    private boolean mUseTwoPane;
    private ProgressBar mProgressBar;
    RecyclerView mRecyclerView;
    View mViewEmpty;
    int mMovieDbIdSelected;
    private FragmentInteractionListener mListener;

    public MovieGridBaseFragment() {
        // required empty constructor
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

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        if (savedInstanceState != null && mUseTwoPane) {
            mMovieDbIdSelected = savedInstanceState.getInt(STATE_MOVIE_SELECTED);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUseTwoPane) {
            outState.putInt(STATE_MOVIE_SELECTED, mMovieDbIdSelected);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_grid);
        mViewEmpty = view.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_grid);
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
        if (!mUseTwoPane) {
            final FragmentActivity activity = getActivity();

            final Intent intent = new Intent(activity, MovieDetailsActivity.class);
            setDetailsIntentExtras(intent, position);

            final String transitionName = getString(R.string.shared_transition_details_poster);
            ViewCompat.setTransitionName(sharedView, transitionName);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, sharedView, transitionName);
            activity.startActivityForResult(intent, REQUEST_MOVIE_DETAILS, options.toBundle());
        } else {
            final Fragment fragment = getDetailsFragment(position);
            // if fragment is null, user selected the one that is currently displayed, hence we
            // don't want to reload it
            if (fragment != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container_details, fragment, FRAGMENT_TWO_PANE_DETAILS)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                mListener.setTwoPaneEmptyViewVisibility(false);
            }
        }
    }

    protected abstract void setDetailsIntentExtras(Intent intent, int position);

    @Nullable
    protected abstract MovieDetailsBaseFragment getDetailsFragment(int position);

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    /**
     * Defines the interaction with the hosting activity
     */
    public interface FragmentInteractionListener {
        void setTwoPaneEmptyViewVisibility(boolean show);
    }
}
