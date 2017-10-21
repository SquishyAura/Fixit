package com.example.nasib.fixit.Entities;

/**
 * Created by doalf on 19-10-2017.
 */

public class Reward {
    public String name;
    public int price; //costs user points
    public String image;

    public Reward(){
        // Default constructor required for calls to DataSnapshot.getValue(Reward.class)
    }

    public Reward(String name, int price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }
}
