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

package ch.berta.fabio.popularmovies.presentation.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.View;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.ui.activities.MovieGridActivity;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModel;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides an abstract base class for the display of movies in a grid of posters.
 */
public abstract class MovieGridBaseFragment<T extends MovieGridViewModel, S extends MovieGridBaseFragment.ActivityListener> extends BaseFragment<T> implements
        MovieGridViewModel.ViewInteractionListener {

    public static final int REQUEST_MOVIE_DETAILS = 1;
    private static final String LOG_TAG = MovieGridBaseFragment.class.getSimpleName();
    S mActivity;
    @Inject
    SharedPreferences mSharedPrefs;
    boolean mUseTwoPane;

    public MovieGridBaseFragment() {
        // required empty constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (S) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
    }

    protected abstract void setupRecyclerView();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity.setViewModel(mViewModel);
    }

    final int getLayoutWidth() {
        int screenWidth = Utils.getScreenWidth(getResources());
        return mUseTwoPane ? screenWidth / 100 *
                getResources().getInteger(R.integer.two_pane_list_width_percentage) : screenWidth;
    }

    final void startDetailsActivity(@NonNull Intent intent, @NonNull View posterSharedElement) {
        final FragmentActivity activity = getActivity();
        final String transitionName = getString(R.string.shared_transition_details_poster);
        ViewCompat.setTransitionName(posterSharedElement, transitionName);
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, posterSharedElement, transitionName);
        activity.startActivityForResult(intent, REQUEST_MOVIE_DETAILS, options.toBundle());
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void persistSort(@NonNull Sort sort, int position) {
        mSharedPrefs.edit().putInt(MovieGridActivity.PERSIST_SORT, position).apply();
    }

    @Override
    public void hideDetailsView() {
        mActivity.hideDetailsFragment();
    }

    final void replaceFragment(@NonNull MovieGridBaseFragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_main, fragment, MovieGridActivity.FRAGMENT_MOVIES)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    final void replaceDetailsFragment(@NonNull MovieDetailsBaseFragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_details, fragment, MovieGridActivity.FRAGMENT_TWO_PANE_DETAILS)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        mViewModel.setUserSelectedMovie(true);
    }

    /**
     * Defines the interaction with the hosting activity
     */
    public interface ActivityListener {

        void setViewModel(@NonNull MovieGridViewModel viewModel);

        void hideDetailsFragment();
    }
}