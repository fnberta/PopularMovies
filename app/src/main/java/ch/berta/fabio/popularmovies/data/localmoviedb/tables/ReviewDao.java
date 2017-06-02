package ch.berta.fabio.popularmovies.data.localmoviedb.tables;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ReviewDao {

    @Query("SELECT r.id, r.movie_id, r.author, r.content, r.url FROM review r " +
            "LEFT JOIN movie m ON m.id = r.movie_id " +
            "WHERE db_id = :movieDbId")
    Flowable<List<ReviewEntity>> getByMovieDbId(int movieDbId);

    @Insert
    void insertAll(List<ReviewEntity> reviews);

    @Query("DELETE FROM review " +
            "WHERE movie_id IN (SELECT id FROM movie m WHERE db_id = :movieDbId)")
    int deleteByMovieDbId(int movieDbId);
}
