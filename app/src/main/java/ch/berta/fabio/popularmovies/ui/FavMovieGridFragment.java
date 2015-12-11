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
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import ch.berta.fabio.popularmovies.ui.adapters.FavMoviesRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.decorators.PosterGridItemDecoration;

/**
 * Displays a grid of movie poster images.
 */
public class FavMovieGridFragment extends BaseMovieGridFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_MOVIE_SELECTED_ROW_ID = "ch.berta.fabio.popularmovies.intents.MOVIE_SELECTED_ROW_ID";
    public static final int COL_INDEX_ROW_ID = 0;
    public static final int COL_INDEX_DB_ID = 1;
    public static final int COL_INDEX_TITLE = 2;
    public static final int COL_INDEX_RELEASE_DATE = 3;
    public static final int COL_INDEX_POSTER = 4;
    private static final String[] FAV_MOVIE_COLUMNS = new String[]{
            BaseColumns._ID,
            MovieContract.Movie.COLUMN_DB_ID,
            MovieContract.Movie.COLUMN_TITLE,
            MovieContract.Movie.COLUMN_RELEASE_DATE,
            MovieContract.Movie.COLUMN_POSTER,
    };
    private static final int FAV_MOVIES_LOADER = 0;
    private FavMoviesRecyclerAdapter mRecyclerAdapter;

    public FavMovieGridFragment() {
        // required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fav_movie_grid, container, false);
    }

    protected void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new FavMoviesRecyclerAdapter(null, mViewEmpty, getLayoutWidth(),
                spanCount, this, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FAV_MOVIES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.Movie.CONTENT_URI,
                FAV_MOVIE_COLUMNS,
                null,
                null,
                MovieContract.Movie.SORT_BY_RELEASE_DATE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
        toggleMainVisibility(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);
    }

    @Override
    protected Intent setDetailsIntentExtras(Intent intent, int position) {
        final long rowId = mRecyclerAdapter.getItemId(position);
        intent.putExtra(INTENT_MOVIE_SELECTED_ROW_ID, rowId);
        return intent;
    }

    @Nullable
    @Override
    protected BaseMovieDetailsFragment getDetailsFragment(int position) {
        final int dbId = mRecyclerAdapter.getMovieDbIdForPosition(position);
        if (mMovieDbIdSelected == dbId) {
            return null;
        }

        mMovieDbIdSelected = dbId;
        final long rowId = mRecyclerAdapter.getItemId(position);
        return FavMovieDetailsFragment.newInstance(rowId);
    }
}
