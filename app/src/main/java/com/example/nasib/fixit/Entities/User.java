package com.example.nasib.fixit.Entities;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created on 25-09-2017.
 */
@IgnoreExtraProperties
public class User {

    public String username;
    public int points;
    public int upvotes;
    public String lastPurchase;

    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, int points, int upvotes, String lastPurchase){
        this.username = username;
        this.points = points;
        this.upvotes = upvotes;
        this.lastPurchase = lastPurchase;

    }

}
