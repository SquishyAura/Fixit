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
import java.util.Collections;
import java.util.List;

public class BoardActivity extends Fragment{
    ListView boardList;

    List<String> descriptionList;
    List<Integer> upvoteList;
    List<String> locationList;
    List<String> statusList;
    List<String> imageList;
    List<String> authorList;

    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        descriptionList = new ArrayList<>();
        upvoteList = new ArrayList<>();
        locationList = new ArrayList<>();
        statusList = new ArrayList<>();
        imageList = new ArrayList<>();
        authorList = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        boardList = (ListView) view.findViewById(R.id.boardListView); //creates a simple list with the layout of fragment_board.xml

        database.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                descriptionList.clear();
                upvoteList.clear();
                locationList.clear();
                statusList.clear();
                imageList.clear();
                authorList.clear();

                for(DataSnapshot post : dataSnapshot.getChildren()){
                    descriptionList.add(post.child("description").getValue().toString());
                    upvoteList.add(Integer.parseInt(post.child("upvotes").getValue().toString()));
                    locationList.add(post.child("location").child("provider").getValue().toString());
                    statusList.add(post.child("status").getValue().toString());
                    imageList.add(post.child("image").getValue().toString());
                    authorList.add(post.child("author").getValue().toString());
                }

                //reverse list in order to display newest post at the top
                Collections.reverse(descriptionList);
                Collections.reverse(upvoteList);
                Collections.reverse(locationList);
                Collections.reverse(statusList);
                Collections.reverse(imageList);
                Collections.reverse(authorList);

                if(getActivity() != null && getContext() != null){
                    BoardCustomAdapter customAdapter = new BoardCustomAdapter(getActivity(), descriptionList, upvoteList, locationList, statusList, imageList, authorList);
                    boardList.setAdapter(customAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}