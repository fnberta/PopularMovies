package ch.berta.fabio.popularmovies.data.localmoviedb.tables;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    Flowable<List<MovieEntity>> getAll();

    @Query("SELECT * FROM movie WHERE db_id = :dbId")
    Flowable<MovieEntity> getByDbId(int dbId);

    @Query("SELECT EXISTS(SELECT id FROM movie WHERE db_id = :dbId)")
    Flowable<Integer> existsByDbId(int dbId);

    @Insert(onConflict = REPLACE)
    long insert(MovieEntity movie);

    @Query("DELETE FROM movie WHERE db_id = :dbId")
    int deleteByDbId(int dbId);

    @Delete
    void delete(MovieEntity movie);
}
