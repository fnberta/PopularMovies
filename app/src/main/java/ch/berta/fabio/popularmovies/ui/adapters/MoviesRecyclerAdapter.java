package ch.berta.fabio.popularmovies.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;

/**
 * Created by fabio on 04.10.15.
 */
public class MoviesRecyclerAdapter extends RecyclerView.Adapter {

    private int mViewResource;
    private List<Movie> mMovies;
    private Fragment mLifecycleFragment;
    private AdapterInteractionListener mListener;

    public MoviesRecyclerAdapter(int viewResource, List<Movie> movies, Fragment fragment,
                                 AdapterInteractionListener listener) {
        mViewResource = viewResource;
        mMovies = movies;
        mLifecycleFragment = fragment;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);
        return new MovieRow(view, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MovieRow movieRow = (MovieRow) holder;
        Movie movie = mMovies.get(position);

        movieRow.setMovie(movie.getTitle(), movie.getReleaseDateFormatted(false), movie.getPosterPath(),
                mLifecycleFragment);
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public interface AdapterInteractionListener {
        void onMovieRowItemClick(int position, View sharedView);
    }

    private static class MovieRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewPoster;
        private TextView mTextViewTitle;
        private TextView mTextViewDate;

        public MovieRow(final View itemView, final AdapterInteractionListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMovieRowItemClick(getAdapterPosition(), mImageViewPoster);
                }
            });

            mImageViewPoster = (ImageView) itemView.findViewById(R.id.iv_poster);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tv_date);
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
}
