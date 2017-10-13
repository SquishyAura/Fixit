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

        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        descriptionListt = new ArrayList<>();
        upvoteListt = new ArrayList<>();
        locationListt = new ArrayList<>();
        statusListt = new ArrayList<>();
        imageListt = new ArrayList<>();
        authorListt = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        simpleList = (ListView) view.findViewById(R.id.boardListView); //creates a simple list with the layout of fragment_board.xml

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
                    authorListt.add(post.child("author").getValue().toString());
                }

                if(getActivity() != null && getContext() != null){
                    BoardCustomAdapter customAdapter = new BoardCustomAdapter(getActivity(), descriptionListt, upvoteListt, locationListt, statusListt, /*imageListt,*/ authorListt);
                    simpleList.setAdapter(customAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}