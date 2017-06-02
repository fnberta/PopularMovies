package ch.berta.fabio.popularmovies.data.localmoviedb.tables;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface VideoDao {

    @Query("SELECT v.id, v.movie_id, v.name, v.key, v.site, v.size, v.type FROM video v " +
            "LEFT JOIN movie m ON m.id = v.movie_id " +
            "WHERE m.db_id = :movieDbId")
    Flowable<List<VideoEntity>> getByMovieDbId(int movieDbId);

    @Insert
    void insertAll(List<VideoEntity> videos);

    @Query("DELETE FROM video " +
            "WHERE movie_id IN (SELECT id FROM movie WHERE db_id = :movieDbId)")
    int deleteByMovieDbId(int movieDbId);
}
