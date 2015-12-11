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

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;

/**
 * Created by fabio on 11.12.15.
 */
public class MovieDetailsRecyclerAdapter extends RecyclerView.Adapter {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_INFO = 1;
    public static final int TYPE_REVIEW = 2;
    public static final int TYPE_VIDEO = 3;
    @LayoutRes
    private static final int VIEW_RESOURCE_INFO = R.layout.row_details_info;
    @LayoutRes
    private static final int VIEW_RESOURCE_REVIEW = R.layout.row_details_review;
    @LayoutRes
    private static final int VIEW_RESOURCE_VIDEO = R.layout.row_details_video;
    private static final int NUMBER_OF_HEADER_ROWS = 2;
    private Context mContext;
    private Fragment mLifecycleFragment;
    private Movie mMovie;
    private List<Review> mReviews;
    private List<Video> mVideos;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER: {
                View view = getView(parent, HeaderRow.VIEW_RESOURCE);
                return new HeaderRow(view);
            }
            case TYPE_INFO: {
                View view = getView(parent, VIEW_RESOURCE_INFO);
                return new InfoRow(view);
            }
            case TYPE_REVIEW: {
                View view = getView(parent, VIEW_RESOURCE_REVIEW);
                return new ReviewRow(view);
            }
            case TYPE_VIDEO: {
                View view = getView(parent, VIEW_RESOURCE_VIDEO);
                return new VideoRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    private View getView(ViewGroup parent, @LayoutRes int viewResource) {
        return LayoutInflater.from(parent.getContext()).inflate(viewResource, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INFO;
        }

        if (position == 1) {
            return TYPE_HEADER;
        }

        final int reviewsSize = mReviews.size();
        if (position > 1 && position < reviewsSize + 2) {
            return TYPE_REVIEW;
        }

        if (position == 2 + reviewsSize) {
            return TYPE_HEADER;
        }

        return TYPE_VIDEO;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) holder;
                if (position == 1) {
                    headerRow.setHeader(mContext.getString(R.string.header_reviews));
                } else {
                    headerRow.setHeader(mContext.getString(R.string.header_trailers));
                }

                break;
            }
            case TYPE_INFO: {
                InfoRow infoRow = (InfoRow) holder;

                break;
            }
            case TYPE_REVIEW: {
                ReviewRow reviewRow = (ReviewRow) holder;

                break;
            }
            case TYPE_VIDEO: {
                VideoRow videoRow = (VideoRow) holder;

                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMovie == null ? 0 : NUMBER_OF_HEADER_ROWS + 1 + mReviews.size() + mVideos.size();
    }

    /**
     * Sets the {@link Movie} for the adapter. Nothing gets displayed until this is set.
     *
     * @param movie the movie to display in the adapter
     */
    public void setMovie(@NonNull Movie movie) {
        mMovie = movie;
        mReviews = movie.getReviews();
        mVideos = movie.getVideos();
    }

    public interface AdapterInteractionListener {
        void onReviewRowItemClick(int position);

        void onVideoRowItemClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that display a header view.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class HeaderRow extends RecyclerView.ViewHolder {

        @LayoutRes
        public static final int VIEW_RESOURCE = R.layout.row_header;
        private TextView mTextViewHeader;

        /**
         * Constructs a new {@link HeaderRow}.
         *
         * @param view the inflated view
         */
        public HeaderRow(@NonNull View view) {
            super(view);

            mTextViewHeader = (TextView) view.findViewById(R.id.tv_header);
        }

        /**
         * Sets the header to the specified string.
         *
         * @param header the header to set
         */
        public void setHeader(@NonNull String header) {
            mTextViewHeader.setText(header);
        }

        /**
         * Hides the whole row.
         */
        public void hideRow() {
            itemView.setVisibility(View.GONE);
        }
    }

    private static class InfoRow extends RecyclerView.ViewHolder {

        public InfoRow(View itemView) {
            super(itemView);
        }
    }

    private static class ReviewRow extends RecyclerView.ViewHolder {

        public ReviewRow(View itemView) {
            super(itemView);
        }
    }

    private static class VideoRow extends RecyclerView.ViewHolder {

        public VideoRow(View itemView) {
            super(itemView);
        }
    }
}
