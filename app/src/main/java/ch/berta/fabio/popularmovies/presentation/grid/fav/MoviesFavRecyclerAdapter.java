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

package ch.berta.fabio.popularmovies.presentation.grid.fav;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Date;

import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding;
import ch.berta.fabio.popularmovies.presentation.common.rows.MovieRow;
import ch.berta.fabio.popularmovies.presentation.grid.items.MovieRowViewModel;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides the adapter for a movie poster images grid.
 */
public class MoviesFavRecyclerAdapter extends RecyclerView.Adapter<MovieRow> {

    private static final String LOG_TAG = MoviesFavRecyclerAdapter.class.getSimpleName();
    private final MovieGridViewModelFav viewModel;
    private final MovieRepository movieRepo;
    private final int itemHeight;
    private Cursor cursor;
    private int rowIdColumn;
    private boolean dataValid;

    public MoviesFavRecyclerAdapter(@Nullable Cursor cursor,
                                    @NonNull MovieGridViewModelFav viewModel,
                                    @NonNull MovieRepository movieRepo,
                                    int layoutWidth, int columnCount) {
        this.cursor = cursor;
        dataValid = cursor != null;
        rowIdColumn = dataValid ? cursor.getColumnIndexOrThrow(BaseColumns._ID) : -1;
        setHasStableIds(true);

        this.movieRepo = movieRepo;
        this.viewModel = viewModel;

        itemHeight = Utils.calcPosterHeight(columnCount, layoutWidth);
    }

    @Override
    public MovieRow onCreateViewHolder(ViewGroup parent, int viewType) {
        RowMovieBinding binding = RowMovieBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new MovieRow(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(MovieRow holder, int position) {
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        String title = movieRepo.getMovieTitleFromFavMoviesCursor(cursor);
        Date date = movieRepo.getMovieReleaseDateFromFavMoviesCursor(cursor);
        String poster = movieRepo.getMoviePosterFromFavMoviesCursor(cursor);

        final RowMovieBinding binding = holder.getBinding();
        final MovieRowViewModel viewModel = binding.getViewModel();
        if (viewModel == null) {
            binding.setViewModel(new MovieRowViewModel(title, date, poster, itemHeight));
        } else {
            viewModel.setMovieInfo(title, date, poster);
        }
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return dataValid ? cursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (dataValid) {
            return cursor.moveToPosition(position)
                   ? cursor.getLong(rowIdColumn)
                   : RecyclerView.NO_ID;
        } else {
            return RecyclerView.NO_ID;
        }
    }

    /**
     * Returns the TheMovieDB id for the movie at position.
     *
     * @param position the position of the movie
     * @return the TheMovieDB id
     */
    public int getMovieDbIdForPosition(int position) {
        if (!cursor.moveToPosition(position)) {
            return -1;
        }

        return movieRepo.getMovieDbIdFromFavMoviesCursor(cursor);
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(@Nullable Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is returned.
     */
    public Cursor swapCursor(@Nullable Cursor newCursor) {
        if (newCursor == cursor) {
            return null;
        }

        final int itemCount = getItemCount();
        final Cursor oldCursor = cursor;
        cursor = newCursor;

        if (newCursor != null) {
            rowIdColumn = newCursor.getColumnIndexOrThrow(BaseColumns._ID);
            dataValid = true;
            notifyDataSetChanged();
        } else {
            rowIdColumn = -1;
            dataValid = false;
            // notify about the lack of a data set
            notifyItemRangeRemoved(0, itemCount);
        }

        return oldCursor;
    }
}
