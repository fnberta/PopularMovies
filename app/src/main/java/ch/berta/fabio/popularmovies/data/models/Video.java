/*
 * Copyright (c) 2015 Fabio Berta
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

package ch.berta.fabio.popularmovies.data.models;

import android.content.ContentValues;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a video (e.g. trailer) of a movie, obtained from TheMovieDb.
 */
public class Video extends BaseObservable implements Parcelable {

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private static final String YOU_TUBE = "YouTube";
    @SerializedName("name")
    private String mName;
    @SerializedName("key")
    private String mKey;
    @SerializedName("site")
    private String mSite;
    @SerializedName("size")
    private int mSize;
    @SerializedName("type")
    private String mType;

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
        mName = in.readString();
        mKey = in.readString();
        mSite = in.readString();
        mSize = in.readInt();
        mType = in.readString();
    }

    @Bindable
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
        notifyPropertyChanged(BR.key);
    }

    @Bindable
    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        mSite = site;
        notifyPropertyChanged(BR.site);
    }

    @Bindable
    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
        notifyPropertyChanged(BR.size);
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public boolean siteIsYouTube() {
        return mSite.equals(YOU_TUBE);
    }

    public String getYoutubeUrl() {
        return Video.YOUTUBE_BASE_URL + getKey();
    }

    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Video.COLUMN_NAME, mName);
        contentValues.put(MovieContract.Video.COLUMN_KEY, mKey);
        contentValues.put(MovieContract.Video.COLUMN_SITE, mSite);
        contentValues.put(MovieContract.Video.COLUMN_SIZE, mSize);
        contentValues.put(MovieContract.Video.COLUMN_TYPE, mType);
        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mKey);
        dest.writeString(mSite);
        dest.writeInt(mSize);
        dest.writeString(mType);
    }
}
