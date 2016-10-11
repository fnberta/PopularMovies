/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.domain.models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a review of a movie, obtained from TheMovieDb.
 */
public class Review implements Parcelable {

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
    private String author;
    private String content;
    private String url;

    public Review() {
    }

    public Review(String author, String content, String url) {
        this.author = author;
        this.content = content;
        this.url = url;
    }

    protected Review(Parcel in) {
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Review.COLUMN_AUTHOR, author);
        contentValues.put(MovieContract.Review.COLUMN_CONTENT, content);
        contentValues.put(MovieContract.Review.COLUMN_URL, url);
        return contentValues;
    }

    @Override
    public String toString() {
        return author + ": " + content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }
}
