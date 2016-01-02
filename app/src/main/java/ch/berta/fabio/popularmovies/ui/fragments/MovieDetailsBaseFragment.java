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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.SnackbarAction;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.ui.adapters.MovieDetailsRecyclerAdapter;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;
import ch.berta.fabio.popularmovies.viewmodels.MovieDetailsViewModel;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;

/**
 * Provides a base class for the display of detail information about a movie, including poster
 * image, release date, rating, an overview of the plot, reviews and trailers.
 */
public abstract class MovieDetailsBaseFragment<T extends MovieDetailsViewModel> extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieDetailsViewModel.ViewInteractionListener {

    static final String KEY_VIEW_MODEL = "KEY_VIEW_MODEL";
    private static final String QUERY_MOVIE_DETAILS_WORKER = "QUERY_MOVIE_DETAILS_WORKER";
    private static final String LOG_TAG = MovieDetailsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mActivity;
    boolean mUseTwoPane;
    T mViewModel;
    RecyclerView mRecyclerView;
    MovieRepository mMovieRepo;
    private MovieDetailsRecyclerAdapter mRecyclerAdapter;
    private Intent mShareYoutubeIntent;

    public MovieDetailsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);
        mMovieRepo = new MovieRepository();

        mShareYoutubeIntent = new Intent(Intent.ACTION_SEND);
        mShareYoutubeIntent.setType("text/plain");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = getRecyclerView();
        mRecyclerAdapter = new MovieDetailsRecyclerAdapter(getActivity(), mViewModel);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    protected abstract RecyclerView getRecyclerView();

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_movie_details, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_details_action_share);
        shareItem.setVisible(mViewModel.hasMovieVideos());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_details_action_share:
                shareYoutubeUrl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareYoutubeUrl() {
        startActivity(Intent.createChooser(mShareYoutubeIntent, getString(R.string.action_share)));
    }

    @Override
    public void onStop() {
        super.onStop();

        mMovieRepo.cancelLocalOperations();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void showSnackbar(@StringRes int text, @Nullable SnackbarAction action) {
        Snackbar snackbar = Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_LONG);
        if (action != null) {
            snackbar.setAction(action.getActionText(), action);
        }
        snackbar.show();
    }

    @Override
    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        mRecyclerAdapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void notifyDataChanged() {
        mRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void loadQueryMovieDetailsWorker(int movieDbId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, QUERY_MOVIE_DETAILS_WORKER);

        if (worker == null) {
            worker = QueryMovieDetailsWorker.newInstance(movieDbId);
            fragmentManager.beginTransaction()
                    .add(worker, QUERY_MOVIE_DETAILS_WORKER)
                    .commit();
        }
    }

    @Override
    public void removeQueryMovieDetailsWorker() {
        WorkerUtils.removeWorker(getFragmentManager(), QUERY_MOVIE_DETAILS_WORKER);
    }

    @Override
    public void invalidateOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setYoutubeShareUrl(@NonNull String url) {
        mShareYoutubeIntent.putExtra(Intent.EXTRA_TEXT, url);
    }

    @Override
    public void deleteMovieLocal() {
        mMovieRepo.deleteMovieLocal(getActivity(), mViewModel);
    }

    @Override
    public void insertMovieLocal() {
        mMovieRepo.insertMovieLocal(getActivity(), mViewModel);
    }

    @Override
    public void startVideoActivity(@NonNull Uri videoUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
        startActivity(intent);
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface FragmentInteractionListener {
        /**
         * Hides the details fragment in a two pane layout (on tablets).
         */
        void hideDetailsFragment();
    }
}
