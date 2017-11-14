package com.example.nasib.fixit.Entities;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by doalf on 14-11-2017.
 */

@IgnoreExtraProperties
public class Position {
    public double latitude;
    public double longitude;

    public Position(){

    }

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
