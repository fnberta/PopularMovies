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

package ch.berta.fabio.popularmovies.data.localmoviedb

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.*

@Database(
        entities = arrayOf(MovieEntity::class, VideoEntity::class, ReviewEntity::class),
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MovieDb : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun reviewDao(): ReviewDao
    abstract fun videoDao(): VideoDao
}


