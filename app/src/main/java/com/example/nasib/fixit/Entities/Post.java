package com.example.nasib.fixit.Entities;

import android.location.Location;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by doalf on 11-10-2017.
 */

@IgnoreExtraProperties
public class Post {
    public String description;
    public String upvotes; //upvotes is a string because we'll be storing who upvoted which post in a list, and then just display the list size as an integer, to show how many upvoted a post
    public Location location;
    public String status;
    public String image;
    public String author;

    public Post(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String description, String upvotes, Location location, String status, String image, String author) {
        this.description = description;
        this.upvotes = upvotes;
        this.location = location;
        this.status = status;
        this.image = image;
        this.author = author;
    }
}
