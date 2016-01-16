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

package ch.berta.fabio.popularmovies.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.databinding.RowMovieBinding;
import ch.berta.fabio.popularmovies.presentation.ui.adapters.rows.MovieRow;
import ch.berta.fabio.popularmovies.presentation.ui.adapters.rows.ProgressRow;
import ch.berta.fabio.popularmovies.utils.Utils;
import ch.berta.fabio.popularmovies.presentation.viewmodels.rows.MovieRowViewModel;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.viewmodels.rows.MovieRowViewModelImpl;

/**
 * Provides the adapter for a movie poster images grid.
 */
public class MoviesOnlRecyclerAdapter extends RecyclerView.Adapter {

    private static final String LOG_TAG = MoviesOnlRecyclerAdapter.class.getSimpleName();
    private final MovieGridViewModelOnl mViewModel;
    private final int mItemHeight;

    public MoviesOnlRecyclerAdapter(@NonNull MovieGridViewModelOnl fragmentViewModel,
                                    int layoutWidth, int columnCount) {
        mViewModel = fragmentViewModel;
        mItemHeight = Utils.calcPosterHeight(columnCount, layoutWidth);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case MovieGridViewModelOnl.TYPE_ITEM: {
                final RowMovieBinding binding = RowMovieBinding.inflate(inflater, parent, false);
                return new MovieRow(binding, mViewModel);
            }
            case MovieGridViewModelOnl.TYPE_PROGRESS: {
                final View view = inflater.inflate(ProgressRow.VIEW_RESOURCE, parent, false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);

        switch (viewType) {
            case MovieGridViewModelOnl.TYPE_ITEM:
                final MovieRow movieRow = (MovieRow) holder;
                final RowMovieBinding binding = movieRow.getBinding();
                final Movie movie = mViewModel.getMovieAtPosition(position);

                final MovieRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    binding.setViewModel(new MovieRowViewModelImpl(movie, mItemHeight));
                } else {
                    viewModel.setMovieInfo(movie);
                }

                binding.executePendingBindings();
                break;
            case MovieGridViewModelOnl.TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
