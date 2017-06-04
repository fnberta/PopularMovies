/*
 * Copyright (c) 2017 Fabio Berta
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

    @Query("SELECT * FROM movie WHERE id = :id")
    Flowable<MovieEntity> getById(int id);

    @Query("SELECT EXISTS(SELECT id FROM movie WHERE id = :id)")
    Flowable<Integer> existsById(int id);

    @Insert(onConflict = REPLACE)
    long insert(MovieEntity movie);

    @Update
    void update(MovieEntity movie);

    @Query("DELETE FROM movie WHERE id = :id")
    int deleteById(int id);
}
