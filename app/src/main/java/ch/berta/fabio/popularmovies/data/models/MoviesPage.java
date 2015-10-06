package ch.berta.fabio.popularmovies.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio on 03.10.15.
 */
public class MoviesPage {

    @SerializedName("page")
    private int mPage;
    @SerializedName("results")
    private List<Movie> mMovies = new ArrayList<>();
    @SerializedName("total_pages")
    private int mTotalPages;
    @SerializedName("total_Movies")
    private int mTotalMovies;

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public List<Movie> getMovies() {
        return mMovies;
    }

    public void setMovies(List<Movie> Movies) {
        mMovies = Movies;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getTotalMovies() {
        return mTotalMovies;
    }

    public void setTotalMovies(int totalMovies) {
        mTotalMovies = totalMovies;
    }

}
