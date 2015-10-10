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
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;

/**
 * Created by fabio on 04.10.15.
 */
public class MoviesRecyclerAdapter extends RecyclerView.Adapter {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_PROGRESS = 1;
    private static final double POSTER_ASPECT_RATIO = 0.675;
    private static final String LOG_TAG = MoviesRecyclerAdapter.class.getSimpleName();
    private int mViewResource;
    private List<Movie> mMovies;
    private Fragment mLifecycleFragment;
    private AdapterInteractionListener mListener;
    private int mItemHeight;

    public MoviesRecyclerAdapter(Context context, int viewResource, List<Movie> movies,
                                 Fragment fragment, AdapterInteractionListener listener) {
        mViewResource = viewResource;
        mMovies = movies;
        mLifecycleFragment = fragment;
        mListener = listener;

        calcPosterHeight(context);
    }

    private void calcPosterHeight(Context context) {
        int columns = context.getResources().getInteger(R.integer.span_count);
        int screenWidth = Utils.getScreenWidth(context);
        int itemWidth = screenWidth / columns;
        mItemHeight = (int) (itemWidth / POSTER_ASPECT_RATIO);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent,
                        false);
                return new MovieRow(view, mItemHeight, mListener);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progress,
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

    public int getLastPosition() {
        return getItemCount() - 1;
    }

    public void setMovies(List<Movie> movies) {
        mMovies.clear();

        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }

        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> movies) {
        mMovies.addAll(movies);
        notifyItemRangeInserted(getItemCount(), movies.size());
    }

    public void showLoadMoreIndicator() {
        mMovies.add(null);
        notifyItemInserted(getLastPosition());
    }

    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mMovies.remove(position);
        notifyItemRemoved(position);
    }

    public interface AdapterInteractionListener {
        void onMovieRowItemClick(int position, View sharedView);
    }

    private static class MovieRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewPoster;
        private TextView mTextViewTitle;
        private TextView mTextViewDate;

        public MovieRow(final View itemView, int itemHeight,
                        final AdapterInteractionListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMovieRowItemClick(getAdapterPosition(), mImageViewPoster);
                }
            });

            setPosterHeight(itemView, itemHeight);
            mImageViewPoster = (ImageView) itemView.findViewById(R.id.iv_poster);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tv_date);
        }

        private void setPosterHeight(View itemView, int itemHeight) {
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = itemHeight;
            itemView.setLayoutParams(layoutParams);
        }

        public void setMovie(String title, String date, String poster, Fragment fragment) {
            if (!TextUtils.isEmpty(poster)) {
                mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                String imageUrl = Movie.IMAGE_BASE_URL + Movie.IMAGE_POSTER_SIZE + poster;
                Glide.with(fragment)
                        .load(imageUrl)
                        .crossFade()
                        .into(mImageViewPoster);
            }
            else {
                mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER);
                mImageViewPoster.setImageResource(R.drawable.ic_movie_white_72dp);
            }
            mTextViewTitle.setText(title);
            mTextViewDate.setText(date);
        }
    }

    private static class ProgressRow extends RecyclerView.ViewHolder {

        private ProgressBar mProgressBar;

        public ProgressRow(View view) {
            super(view);

            mProgressBar = (ProgressBar) view.findViewById(R.id.pb_more);
        }
    }
}
