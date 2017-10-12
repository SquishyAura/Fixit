package com.example.nasib.fixit;

/**
 * Created by nasib on 28-09-2017.
 */

import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends Fragment{
    ListView simpleList;
    String descriptionList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand", "China", "australia", "Portugle", "America", "NewZealand"};
    int upvoteList[] = {3, 1, 8, 2, 6, 4, 1, 8, 2, 6, 4};
    int distanceList[] = {100, 251, 900, 1052, 240, 810, 251, 900, 1052, 240, 810};

    int size;
    List<String> descriptionListt;
    List<Integer> upvoteListt;
    List<String> locationListt;
    List<String> statusListt;
    List<String> imageListt;
    List<String> authorListt;


    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        size = 1;
        descriptionListt = new ArrayList<>();
        upvoteListt = new ArrayList<>();
        locationListt = new ArrayList<>();
        statusListt = new ArrayList<>();
        imageListt = new ArrayList<>();
        authorListt = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        /* TEMPORARY CODE TO MAKE A POST
        final Post post = new Post("this is a description", 0, new Location("rawr"), "Solved", "ugabugaimage", new User("TESTUSER", 15, 10, "2017-09-08"));
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                database.child("posts").push().setValue(post);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/



        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        simpleList = (ListView) rootView.findViewById(R.id.boardListView); //creates a simple list with the layout of fragment_board.xml

        database.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                descriptionListt.clear();
                upvoteListt.clear();
                locationListt.clear();
                statusListt.clear();
                imageListt.clear();
                authorListt.clear();

                for(DataSnapshot post : dataSnapshot.getChildren()){
                    descriptionListt.add(post.child("description").getValue().toString());
                    upvoteListt.add(Integer.parseInt(post.child("upvotes").getValue().toString()));
                    locationListt.add(post.child("location").child("provider").getValue().toString());
                    statusListt.add(post.child("status").getValue().toString());
                    imageListt.add(post.child("image").getValue().toString());
                    authorListt.add(post.child("author").child("username").getValue().toString());
                }

                BoardCustomAdapter customAdapter = new BoardCustomAdapter(getActivity().getApplicationContext(), descriptionListt, upvoteListt, locationListt, statusListt, /*imageListt,*/ authorListt);
                simpleList.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }
}