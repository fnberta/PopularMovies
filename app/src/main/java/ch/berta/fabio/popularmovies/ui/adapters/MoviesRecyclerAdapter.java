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

package ch.berta.fabio.popularmovies.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.ui.adapters.listeners.MovieInteractionListener;
import ch.berta.fabio.popularmovies.ui.adapters.rows.MovieRow;

/**
 * Provides the adapter for a movie poster images grid.
 */
public class MoviesRecyclerAdapter extends RecyclerView.Adapter {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_PROGRESS = 1;
    private static final int VIEW_RESOURCE_ITEM = R.layout.row_movie;
    private static final int VIEW_RESOURCE_PROGRESS = R.layout.row_progress;
    private static final double POSTER_ASPECT_RATIO = 0.675;
    private static final String LOG_TAG = MoviesRecyclerAdapter.class.getSimpleName();
    private List<Movie> mMovies;
    private View mViewEmpty;
    private Fragment mLifecycleFragment;
    private MovieInteractionListener mListener;
    private int mItemHeight;

    public MoviesRecyclerAdapter(List<Movie> movies, View emptyView, int layoutWidth, int columnCount,
                                 Fragment fragment, MovieInteractionListener listener) {
        mMovies = movies;
        mViewEmpty = emptyView;
        mLifecycleFragment = fragment;
        mListener = listener;

        mItemHeight = calcPosterHeight(columnCount, layoutWidth);
    }

    public static int calcPosterHeight(int columns, int layoutWidth) {
        int itemWidth = layoutWidth / columns;
        return (int) (itemWidth / POSTER_ASPECT_RATIO);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_ITEM,
                        parent, false);
                return new MovieRow(view, mItemHeight, mListener);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_PROGRESS,
                        parent, false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_ITEM:
                MovieRow movieRow = (MovieRow) holder;
                Movie movie = mMovies.get(position);

                movieRow.setMovie(movie.getTitle(), movie.getReleaseDateFormatted(false),
                        movie.getPosterPath(), mLifecycleFragment);
                break;
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMovies.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    /**
     * Returns the position of the last movie in the adapter.
     *
     * @return the position of the last movie
     */
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    /**
     * Clears all movies from the adapter and sets new ones.
     *
     * @param movies the movies to be set
     */
    public void setMovies(List<Movie> movies) {
        mMovies.clear();

        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }

        notifyDataSetChanged();
        toggleEmptyViewVisibility();
    }

    /**
     * Sets the visibility of the empty view depending on whether the item count is bigger than
     * zero or not.
     */
    public void toggleEmptyViewVisibility() {
        mViewEmpty.setVisibility(getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    /**
     * Adds movies to the adapter.
     *
     * @param movies the movies to be added
     */
    public void addMovies(List<Movie> movies) {
        mMovies.addAll(movies);
        notifyItemRangeInserted(getItemCount(), movies.size());
    }

    /**
     * Shows a progressbar in the last row as an indicator that more objects are being fetched.
     */
    public void showLoadMoreIndicator() {
        mMovies.add(null);
        notifyItemInserted(getLastPosition());
    }

    /**
     * Hides the progressbar in the last row.
     */
    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mMovies.remove(position);
        notifyItemRemoved(position);
    }

    private static class ProgressRow extends RecyclerView.ViewHolder {

        public ProgressRow(View view) {
            super(view);
        }
    }
}
