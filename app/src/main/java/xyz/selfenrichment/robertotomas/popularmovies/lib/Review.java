package xyz.selfenrichment.robertotomas.popularmovies.lib;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/** Parcelable Review POJO for TMDB query returns */
public class Review implements Parcelable {

    private String id_of_movie;
    private String id;
    private String author;
    private String content;
    private String url;

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel source) {
            Review innerReview = new Review();
            innerReview.id_of_movie = source.readString();
            innerReview.id = source.readString();
            innerReview.author = source.readString();
            innerReview.content = source.readString();
            innerReview.url = source.readString();
            return innerReview;
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id_of_movie);
        parcel.writeString(id);
        parcel.writeString(author);
        parcel.writeString(content);
        parcel.writeString(url);
    }

    public Review() {
    }

    public Review(Map<String, String> mapMovie) {
        id_of_movie = mapMovie.get("id_of_movie");
        id = mapMovie.get("id");
        author = mapMovie.get("author");
        content = mapMovie.get("content");
        url = mapMovie.get("url");
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_of_movie() {
        return id_of_movie;
    }

    public void setId_of_movie(String id_of_movie) {
        this.id_of_movie = id_of_movie;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}