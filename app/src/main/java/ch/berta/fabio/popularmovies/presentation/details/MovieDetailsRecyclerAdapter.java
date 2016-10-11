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

package ch.berta.fabio.popularmovies.presentation.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.domain.models.Review;
import ch.berta.fabio.popularmovies.domain.models.Video;
import ch.berta.fabio.popularmovies.databinding.RowDetailsInfoBinding;
import ch.berta.fabio.popularmovies.databinding.RowDetailsReviewBinding;
import ch.berta.fabio.popularmovies.databinding.RowDetailsTwoPaneHeaderBinding;
import ch.berta.fabio.popularmovies.databinding.RowDetailsVideoBinding;
import ch.berta.fabio.popularmovies.databinding.RowHeaderBinding;
import ch.berta.fabio.popularmovies.presentation.common.rows.BaseBindingRow;
import ch.berta.fabio.popularmovies.presentation.common.rows.HeaderRow;
import ch.berta.fabio.popularmovies.presentation.details.items.DetailsHeaderRowViewModel;
import ch.berta.fabio.popularmovies.presentation.details.items.DetailsInfoRowViewModel;
import ch.berta.fabio.popularmovies.presentation.details.items.DetailsReviewRowViewModel;

/**
 * Provides the adapter to display the details of a movie.
 */
public class MovieDetailsRecyclerAdapter extends RecyclerView.Adapter {

    private final Context mContext;
    private final MovieDetailsViewModel mViewModel;

    /**
     * Returns a new instance of a {@link MovieDetailsRecyclerAdapter}.
     *
     * @param context   the context to use in the adapter
     * @param viewModel the viewModel of the fragment
     */
    public MovieDetailsRecyclerAdapter(@NonNull Context context,
                                       @NonNull MovieDetailsViewModel viewModel) {
        mContext = context;
        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case MovieDetailsViewModel.TYPE_HEADER: {
                final RowHeaderBinding binding = RowHeaderBinding.inflate(inflater, parent, false);
                return new HeaderRow(binding);
            }
            case MovieDetailsViewModel.TYPE_TWO_PANE_HEADER: {
                final RowDetailsTwoPaneHeaderBinding binding =
                        RowDetailsTwoPaneHeaderBinding.inflate(inflater, parent, false);
                return new TwoPaneHeaderRow(binding);
            }
            case MovieDetailsViewModel.TYPE_INFO: {
                final RowDetailsInfoBinding binding =
                        RowDetailsInfoBinding.inflate(inflater, parent, false);
                return new InfoRow(binding);
            }
            case MovieDetailsViewModel.TYPE_REVIEW: {
                final RowDetailsReviewBinding binding =
                        RowDetailsReviewBinding.inflate(inflater, parent, false);
                return new ReviewRow(binding);
            }
            case MovieDetailsViewModel.TYPE_VIDEO: {
                final RowDetailsVideoBinding binding =
                        RowDetailsVideoBinding.inflate(inflater, parent, false);
                return new VideoRow(binding, mViewModel);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case MovieDetailsViewModel.TYPE_HEADER: {
                final HeaderRow headerRow = (HeaderRow) holder;
                final RowHeaderBinding binding = headerRow.getBinding();

                final int header = mViewModel.getHeaderTitle(position);
                final DetailsHeaderRowViewModel viewModel = new DetailsHeaderRowViewModel(header);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();

                break;
            }
            case MovieDetailsViewModel.TYPE_TWO_PANE_HEADER: {
                final TwoPaneHeaderRow twoPaneHeaderRow = (TwoPaneHeaderRow) holder;
                final RowDetailsTwoPaneHeaderBinding binding = twoPaneHeaderRow.getBinding();
                binding.setMovie(mViewModel.getMovie());
                binding.executePendingBindings();

                break;
            }
            case MovieDetailsViewModel.TYPE_INFO: {
                final InfoRow infoRow = (InfoRow) holder;
                final int plotMaxLines = mContext.getResources().getInteger(R.integer.plot_max_lines);
                final DetailsInfoRowViewModel viewModel =
                        new DetailsInfoRowViewModel(mViewModel.getMovie(), plotMaxLines);

                final RowDetailsInfoBinding binding = infoRow.getBinding();
                binding.setViewModel(viewModel);
                binding.setDetailsListener(mViewModel);
                binding.executePendingBindings();

                break;
            }
            case MovieDetailsViewModel.TYPE_REVIEW: {
                final ReviewRow reviewRow = (ReviewRow) holder;
                final Review review = mViewModel.getMovieReviewAtPosition(position);
                final int contentMaxLines = mContext.getResources().getInteger(R.integer.review_content_max_lines);
                final DetailsReviewRowViewModel viewModel = new DetailsReviewRowViewModel(review,
                        mViewModel.isMovieReviewLastPosition(review), contentMaxLines);

                final RowDetailsReviewBinding binding = reviewRow.getBinding();
                binding.setViewModel(viewModel);
                binding.executePendingBindings();

                break;
            }
            case MovieDetailsViewModel.TYPE_VIDEO: {
                final VideoRow videoRow = (VideoRow) holder;
                final Video video = mViewModel.getMovieVideoAtPosition(position);

                final RowDetailsVideoBinding binding = videoRow.getBinding();
                binding.setVideo(video);
                binding.executePendingBindings();

                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    /**
     * Provides a {@link RecyclerView} row that displays the title and backdrop image of a movie.
     * Only used in two pane setups, e.g. on tables.
     * <p/>
     * Subclass of {@link BaseBindingRow}.
     */
    private static class TwoPaneHeaderRow extends BaseBindingRow<RowDetailsTwoPaneHeaderBinding> {

        /**
         * Constructs a new {@link TwoPaneHeaderRow}.
         *
         * @param binding the binding to use
         */
        public TwoPaneHeaderRow(@NonNull RowDetailsTwoPaneHeaderBinding binding) {
            super(binding);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays basic information about a movie.
     * <p/>
     * Subclass of {@link BaseBindingRow}.
     */
    public static class InfoRow extends BaseBindingRow<RowDetailsInfoBinding> {

        /**
         * Constructs a new {@link InfoRow}.
         *
         * @param binding the binding to use
         */
        public InfoRow(@NonNull RowDetailsInfoBinding binding) {
            super(binding);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays reviews about a movie.
     * <p/>
     * Subclass of {@link BaseBindingRow}.
     */
    public static class ReviewRow extends BaseBindingRow<RowDetailsReviewBinding> {

        /**
         * Constructs a new {@link ReviewRow}.
         *
         * @param binding the binding to use
         */
        public ReviewRow(@NonNull RowDetailsReviewBinding binding) {
            super(binding);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays videos (e.g. trailers) about a movie.
     * <p/>
     * Subclass of {@link BaseBindingRow}.
     */
    private static class VideoRow extends BaseBindingRow<RowDetailsVideoBinding> {

        /**
         * Constructs a new {@link VideoRow}.
         *
         * @param binding  the binding to use
         * @param listener the callback for clicks on a video
         */
        public VideoRow(@NonNull RowDetailsVideoBinding binding,
                        @NonNull final MovieDetailsInteractionListener listener) {
            super(binding);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onVideoRowItemClick(getAdapterPosition());
                }
            });
        }
    }
}
