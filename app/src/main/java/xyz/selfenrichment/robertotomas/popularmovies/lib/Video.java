package xyz.selfenrichment.robertotomas.popularmovies.lib;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;


/** Parcelable movie Video POJO for tmdb query results */
public class Video implements Parcelable {

    private String id_of_movie;
    private String source;
    private String name;
    private String size;
    private String type;

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel source) {
            Video innerVideo = new Video();
            innerVideo.id_of_movie = source.readString();
            innerVideo.source = source.readString();
            innerVideo.name = source.readString();
            innerVideo.size = source.readString();
            innerVideo.type = source.readString();
            return innerVideo;
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id_of_movie);
        parcel.writeString(source);
        parcel.writeString(name);
        parcel.writeString(size);
        parcel.writeString(type);
    }

    public Video() {
    }

    public Video(Map<String, String> mapTrailer) {
        id_of_movie = mapTrailer.get("id_of_movie");
        source = mapTrailer.get("source");
        name = mapTrailer.get("name");
        size = mapTrailer.get("size");
        type = mapTrailer.get("type");
    }

    public String getId_of_movie() {
        return id_of_movie;
    }

    public void setId_of_movie(String id_of_movie) {
        this.id_of_movie = id_of_movie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}