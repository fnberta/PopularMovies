package ch.berta.fabio.popularmovies.data.localmoviedb.tables;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ReviewDao {

    @Query("SELECT id, movie_id, author, content, url FROM review " +
            "WHERE movie_id = :movieId")
    Flowable<List<ReviewEntity>> getByMovieId(int movieId);

    @Insert
    void insertAll(List<ReviewEntity> reviews);

    @Query("DELETE FROM review " +
            "WHERE movie_id IN (SELECT id FROM movie m WHERE id = :movieId)")
    int deleteByMovieId(int movieId);
}
