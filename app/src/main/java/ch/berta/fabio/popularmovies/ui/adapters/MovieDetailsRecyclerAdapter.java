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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;

/**
 * Provides the adapter to display the details of a movie.
 */
public class MovieDetailsRecyclerAdapter extends RecyclerView.Adapter {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TWO_PANE_HEADER = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_REVIEW = 3;
    public static final int TYPE_VIDEO = 4;
    @LayoutRes
    private static final int VIEW_RESOURCE_TWO_PANE_HEADER = R.layout.row_details_two_pane_header;
    @LayoutRes
    private static final int VIEW_RESOURCE_INFO = R.layout.row_details_info;
    @LayoutRes
    private static final int VIEW_RESOURCE_REVIEW = R.layout.row_details_review;
    @LayoutRes
    private static final int VIEW_RESOURCE_VIDEO = R.layout.row_details_video;
    private Context mContext;
    private Fragment mLifecycleFragment;
    private AdapterInteractionListener mListener;
    private boolean mUseTwoPane;
    private Movie mMovie;
    private List<Review> mReviews;
    private List<Video> mVideos;
    private int mReviewsCount;
    private int mVideosCount;

    /**
     * Returns a new instance of a {@link MovieDetailsRecyclerAdapter} without a movie specified.
     *
     * @param context           the context to use in the adapter
     * @param useTwoPane        whether the devices uses a two pane view or not
     * @param lifecycleFragment the fragment to use to control the lifecycle of the image loading
     * @param listener          the callback for click and other events
     */
    public MovieDetailsRecyclerAdapter(@NonNull Context context, boolean useTwoPane,
                                       @NonNull Fragment lifecycleFragment,
                                       @NonNull AdapterInteractionListener listener) {
        mContext = context;
        mUseTwoPane = useTwoPane;
        mLifecycleFragment = lifecycleFragment;
        mListener = listener;
    }

    /**
     * Returns a new instance of a {@link MovieDetailsRecyclerAdapter} with a movie specified.
     *
     * @param context           the context to use in the adapter
     * @param movie             the movie to display in the adapter
     * @param useTwoPane        whether the devices uses a two pane view or not
     * @param lifecycleFragment the fragment to use to control the lifecycle of the image loading
     * @param listener          the callback for click and other events
     */
    public MovieDetailsRecyclerAdapter(@NonNull Context context, @NonNull Movie movie,
                                       boolean useTwoPane,
                                       @NonNull Fragment lifecycleFragment,
                                       @NonNull AdapterInteractionListener listener) {
        mContext = context;
        mMovie = movie;
        setReviewsAndVideos();
        mUseTwoPane = useTwoPane;
        mLifecycleFragment = lifecycleFragment;
        mListener = listener;
    }

    private void setReviewsAndVideos() {
        mReviews = mMovie.getReviews();
        mVideos = mMovie.getVideos();
        mReviewsCount = mReviews.size();
        mVideosCount = mVideos.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER: {
                View view = getView(parent, HeaderRow.VIEW_RESOURCE);
                return new HeaderRow(view);
            }
            case TYPE_TWO_PANE_HEADER: {
                View view = getView(parent, VIEW_RESOURCE_TWO_PANE_HEADER);
                return new TwoPaneHeaderRow(view);
            }
            case TYPE_INFO: {
                View view = getView(parent, VIEW_RESOURCE_INFO);
                return new InfoRow(view, mContext.getResources());
            }
            case TYPE_REVIEW: {
                View view = getView(parent, VIEW_RESOURCE_REVIEW);
                return new ReviewRow(view, mContext.getResources());
            }
            case TYPE_VIDEO: {
                View view = getView(parent, VIEW_RESOURCE_VIDEO);
                return new VideoRow(view, mListener);
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
            if (mUseTwoPane) {
                return TYPE_TWO_PANE_HEADER;
            } else {
                return TYPE_INFO;
            }
        }

        if (position == 1) {
            if (mUseTwoPane) {
                return TYPE_INFO;
            } else {
                return TYPE_HEADER;
            }
        }

        if (position == 2 && mUseTwoPane) {
            return TYPE_HEADER;
        }

        if (mReviewsCount > 0) {
            final int firstReviewPos = adjustPosForTwoPane(2);
            if (position >= firstReviewPos && position < firstReviewPos + mReviewsCount) {
                return TYPE_REVIEW;
            }

            if (position == firstReviewPos + mReviewsCount) {
                return TYPE_HEADER;
            }
        }

        return TYPE_VIDEO;
    }

    private int adjustPosForTwoPane(int position) {
        return mUseTwoPane ? position + 1 : position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_HEADER: {
                final HeaderRow headerRow = (HeaderRow) holder;
                if (position != adjustPosForTwoPane(1)) {
                    headerRow.setHeader(mContext.getString(R.string.header_trailers));
                } else if (mReviewsCount == 0) {
                    headerRow.setHeader(mContext.getString(R.string.header_trailers));
                } else {
                    headerRow.setHeader(mContext.getString(R.string.header_reviews));
                }

                break;
            }
            case TYPE_TWO_PANE_HEADER: {
                final TwoPaneHeaderRow twoPaneHeaderRow = (TwoPaneHeaderRow) holder;
                twoPaneHeaderRow.setTwoPaneHeader(mMovie.getTitle(), mMovie.getBackdropPath(),
                        mLifecycleFragment);

                break;
            }
            case TYPE_INFO: {
                final InfoRow infoRow = (InfoRow) holder;

                infoRow.setPoster(mMovie.getPosterPath(), mLifecycleFragment, mListener);
                infoRow.setDate(mMovie.getReleaseDate());
                infoRow.setPlot(mMovie.getOverview());
                infoRow.setRating(mContext.getString(R.string.details_rating, mMovie.getVoteAverage()));

                break;
            }
            case TYPE_REVIEW: {
                final ReviewRow reviewRow = (ReviewRow) holder;
                final Review review = getReviewAtPosition(position);

                reviewRow.setAuthor(review.getAuthor());
                reviewRow.setContent(review.getContent());
                if (mReviews.indexOf(review) == mReviewsCount - 1) {
                    reviewRow.hideDivider();
                } else {
                    reviewRow.showDivider();
                }

                break;
            }
            case TYPE_VIDEO: {
                final VideoRow videoRow = (VideoRow) holder;
                final Video video = getVideoAtPosition(position);

                videoRow.setThumbnail(mContext.getString(R.string.youtube_thumb_url, video.getKey()),
                        mLifecycleFragment);
                videoRow.setName(video.getName());
                videoRow.setSite(video.getSite());
                videoRow.setSize(video.getSize());

                break;
            }
        }
    }

    /**
     * Returns the {@link Review} object at the specified position.
     *
     * @param position the position of the review to get
     * @return the {@link Review} object at the specified position
     */
    public Review getReviewAtPosition(int position) {
        // position - review header - info and two pane header if present
        return mReviews.get(position - 1 - adjustPosForTwoPane(1));
    }

    /**
     * Returns the {@link Video} object at the specified position.
     *
     * @param position the position of the video to get
     * @return the {@link Video} object at the specified position
     */
    public Video getVideoAtPosition(int position) {
        // position - video header and review header if there are reviews - reviews if there are any - info and two pane header if present
        return mVideos.get(position - getNumberOfHeaderRows() - mReviewsCount - adjustPosForTwoPane(1));
    }

    private int getNumberOfHeaderRows() {
        int headerRows = 2;
        if (mReviewsCount == 0) {
            headerRows--;
        }
        if (mVideosCount == 0) {
            headerRows--;
        }
        return headerRows;
    }

    @Override
    public int getItemCount() {
        if (mMovie == null) {
            return 0;
        }

        final int count = 1 + mReviewsCount + mVideosCount + getNumberOfHeaderRows();
        return adjustPosForTwoPane(count);
    }

    /**
     * Sets the reviews and videos and dispatches item range inserted notification.
     */
    public void notifyReviewsAndVideosLoaded() {
        setReviewsAndVideos();
        notifyItemRangeInserted(adjustPosForTwoPane(1),
                getNumberOfHeaderRows() + mReviewsCount + mVideosCount);
    }

    /**
     * Sets the {@link Movie} for the adapter. Nothing gets displayed until this is set.
     *
     * @param movie the movie to display in the adapter
     */
    public void setMovie(@NonNull Movie movie) {
        mMovie = movie;
        setReviewsAndVideos();
        notifyDataSetChanged();
    }

    /**
     * Defines the interaction of the adapter.
     */
    public interface AdapterInteractionListener {
        /**
         * Called when the user clicks on a video row.
         *
         * @param position the position of the row clicked
         */
        void onVideoRowItemClick(int position);

        /**
         * Called when the movie poster finished loading.
         */
        void onPosterLoaded();
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
    }

    /**
     * Provides a {@link RecyclerView} row that displays the title and backdrop image of a movie.
     * Only used in two pane setups, e.g. on tables.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TwoPaneHeaderRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewTwoPaneHeaderBackdrop;
        private TextView mTextViewTwoPaneHeaderTitle;

        /**
         * Constructs a new {@link TwoPaneHeaderRow}.
         *
         * @param itemView the view to inflate
         */
        public TwoPaneHeaderRow(@NonNull View itemView) {
            super(itemView);

            mImageViewTwoPaneHeaderBackdrop = (ImageView) itemView.findViewById(R.id.iv_details_backdrop);
            mTextViewTwoPaneHeaderTitle = (TextView) itemView.findViewById(R.id.tv_details_title);
        }

        public void setTwoPaneHeader(@NonNull String title, @NonNull String backdrop,
                                     @NonNull Fragment lifecycleFragment) {
            mTextViewTwoPaneHeaderTitle.setText(title);

            if (!TextUtils.isEmpty(backdrop)) {
                String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_BACKDROP_SIZE + backdrop;
                Glide.with(lifecycleFragment)
                        .load(imagePath)
                        .into(mImageViewTwoPaneHeaderBackdrop);
            }
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays basic information about a movie.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    public static class InfoRow extends RecyclerView.ViewHolder {

        private final int mPlotMaxLines;
        private ImageView mImageViewPoster;
        private TextView mTextViewPlot;
        private TextView mTextViewDate;
        private TextView mTextViewRating;

        /**
         * Constructs a new {@link InfoRow}.
         *
         * @param itemView the view to inflate
         * @param res      the {@link Resources} to access Android resources in the row
         */
        public InfoRow(@NonNull View itemView, @NonNull Resources res) {
            super(itemView);

            mPlotMaxLines = res.getInteger(R.integer.plot_max_lines);
            mImageViewPoster = (ImageView) itemView.findViewById(R.id.iv_details_poster);
            mTextViewPlot = (TextView) itemView.findViewById(R.id.tv_details_plot);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tv_details_release_date);
            mTextViewRating = (TextView) itemView.findViewById(R.id.tv_details_rating);

            mTextViewPlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.expandOrCollapseTextView(mTextViewPlot, mPlotMaxLines);
                }
            });
        }

        /**
         * Loads the poster image of the movie and calls back when it's loaded.
         *
         * @param poster            the url of the poster to load
         * @param lifecycleFragment the fragment to use to control the lifecycle of the image loading
         * @param listener          the callback for when the poster is loaded
         */
        public void setPoster(@NonNull String poster, @NonNull Fragment lifecycleFragment,
                              @NonNull final AdapterInteractionListener listener) {
            if (!TextUtils.isEmpty(poster)) {
                String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_POSTER_SIZE + poster;
                Glide.with(lifecycleFragment)
                        .load(imagePath)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewPoster) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                super.onResourceReady(resource, glideAnimation);
                                listener.onPosterLoaded();
                            }
                        });
            } else {
                mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER);
                mImageViewPoster.setImageResource(R.drawable.ic_movie_white_72dp);
                listener.onPosterLoaded();
            }
        }

        /**
         * Removes the transition name on the poster image view.
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void removeSharedElement() {
            mImageViewPoster.setTransitionName(null);
        }

        /**
         * Sets the date or removes the view if there is none.
         *
         * @param date the date to set
         */
        public void setDate(@Nullable Date date) {
            String dateFormatted = Utils.formatDateLong(date);
            if (!TextUtils.isEmpty(dateFormatted)) {
                mTextViewDate.setText(dateFormatted);
            } else {
                mTextViewDate.setVisibility(View.GONE);
            }
        }

        public void setPlot(@NonNull String plot) {
            mTextViewPlot.setText(plot);
        }

        public void setRating(@NonNull String rating) {
            mTextViewRating.setText(rating);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays reviews about a movie.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class ReviewRow extends RecyclerView.ViewHolder {

        private final int mContentMaxLines;
        private TextView mTextViewAuthor;
        private TextView mTextViewContent;
        private View mDivider;

        /**
         * Constructs a new {@link ReviewRow}.
         *
         * @param itemView the view to inflate
         * @param res      the {@link Resources} to access Android resources
         */
        public ReviewRow(@NonNull View itemView, @NonNull Resources res) {
            super(itemView);

            mContentMaxLines = res.getInteger(R.integer.review_content_max_lines);
            mTextViewAuthor = (TextView) itemView.findViewById(R.id.tv_details_review_author);
            mTextViewContent = (TextView) itemView.findViewById(R.id.tv_details_review_content);
            mDivider = itemView.findViewById(R.id.v_details_review_divider);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.expandOrCollapseTextView(mTextViewContent, mContentMaxLines);
                }
            });
        }

        public void setAuthor(@NonNull String author) {
            mTextViewAuthor.setText(author);
        }

        public void setContent(@NonNull String content) {
            mTextViewContent.setText(content);
        }

        public void showDivider() {
            mDivider.setVisibility(View.VISIBLE);
        }

        public void hideDivider() {
            mDivider.setVisibility(View.GONE);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays videos (e.g. trailers) about a movie.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class VideoRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewThumb;
        private TextView mTextViewName;
        private TextView mTextViewSite;
        private TextView mTextViewSize;

        /**
         * Constructs a new {@link VideoRow}.
         *
         * @param itemView the view to inflate
         * @param listener the callback for click events
         */
        public VideoRow(@NonNull View itemView,
                        @NonNull final AdapterInteractionListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onVideoRowItemClick(getAdapterPosition());
                }
            });

            mImageViewThumb = (ImageView) itemView.findViewById(R.id.iv_details_video_thumb);
            mTextViewName = (TextView) itemView.findViewById(R.id.tv_details_video_name);
            mTextViewSite = (TextView) itemView.findViewById(R.id.tv_details_video_site);
            mTextViewSize = (TextView) itemView.findViewById(R.id.tv_details_video_size);
        }

        /**
         * Loads the thumbnail image of the video.
         *
         * @param thumbnail         the url of the thumbnail
         * @param lifecycleFragment the fragment to control the lifecycle of the image loading
         */
        public void setThumbnail(@NonNull String thumbnail, @NonNull Fragment lifecycleFragment) {
            Glide.with(lifecycleFragment)
                    .load(thumbnail)
                    .into(mImageViewThumb);
        }

        public void setName(@NonNull String name) {
            mTextViewName.setText(name);
        }

        public void setSite(@NonNull String site) {
            mTextViewSite.setText(site);
        }

        public void setSize(int size) {
            mTextViewSize.setText(String.format("%sp", size));
        }
    }
}
