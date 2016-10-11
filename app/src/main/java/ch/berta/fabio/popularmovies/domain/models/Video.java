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
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a video (e.g. trailer) of a movie, obtained from TheMovieDb.
 */
public class Video extends BaseObservable implements Parcelable {

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private static final String YOU_TUBE = "YouTube";
    private String name;
    private String key;
    private String site;
    private int size;
    private String type;

    public Video() {
    }

    public Video(String name, String key, String site, int size, String type) {
        setName(name);
        setKey(key);
        setSite(site);
        setSize(size);
        setType(type);
    }

    protected Video(Parcel in) {
        name = in.readString();
        key = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        notifyPropertyChanged(BR.key);
    }

    @Bindable
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
        notifyPropertyChanged(BR.site);
    }

    @Bindable
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        notifyPropertyChanged(BR.size);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean siteIsYouTube() {
        return site.equals(YOU_TUBE);
    }

    public String getYoutubeUrl() {
        return Video.YOUTUBE_BASE_URL + getKey();
    }

    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Video.COLUMN_NAME, name);
        contentValues.put(MovieContract.Video.COLUMN_KEY, key);
        contentValues.put(MovieContract.Video.COLUMN_SITE, site);
        contentValues.put(MovieContract.Video.COLUMN_SIZE, size);
        contentValues.put(MovieContract.Video.COLUMN_TYPE, type);
        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
        dest.writeString(site);
        dest.writeInt(size);
        dest.writeString(type);
    }
}
