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

package ch.berta.fabio.popularmovies.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides an abstract base class for the display of movies in a grid of posters.
 */
public abstract class MovieGridBaseFragment extends Fragment {

    public static final int REQUEST_MOVIE_DETAILS = 1;
    static final String STATE_VIEW_MODEL = "STATE_VIEW_MODEL";
    static final String KEY_VIEW_MODEL = "KEY_VIEW_MODEL";
    private static final String LOG_TAG = MovieGridBaseFragment.class.getSimpleName();
    boolean mUseTwoPane;

    public MovieGridBaseFragment() {
        // required empty constructor
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
}
