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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of videos (e.g.) trailers obtained from TheMovieDb.
 */
public class VideosPage implements Parcelable {

    public static final Parcelable.Creator<VideosPage> CREATOR = new Parcelable.Creator<VideosPage>() {
        @Override
        public VideosPage createFromParcel(Parcel source) {
            return new VideosPage(source);
        }

        @Override
        public VideosPage[] newArray(int size) {
            return new VideosPage[size];
        }
    };
    @SerializedName("results")
    private List<Video> videos = new ArrayList<>();

    public VideosPage() {
    }

    protected VideosPage(Parcel in) {
        videos = new ArrayList<>();
        in.readList(videos, List.class.getClassLoader());
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(videos);
    }
}
