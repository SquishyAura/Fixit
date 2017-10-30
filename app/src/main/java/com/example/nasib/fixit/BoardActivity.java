package com.example.nasib.fixit;

/**
 * Created by nasib on 28-09-2017.
 */


import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
    BoardCustomAdapter customAdapter;

    List<String> descriptionList;
    List<String> upvoteList;
    List<Location> locationList;
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
                    upvoteList.add(post.child("upvotes").getChildrenCount() + "");
                    String[] locationString = post.child("location").getValue().toString().split(",");
                    String[] longitude = locationString[4].split("=");
                    String[] latitude = locationString[5].split("=");
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLongitude(Double.parseDouble(longitude[1]));
                    location.setLatitude(Double.parseDouble(latitude[1]));
                    locationList.add(location);
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

                customAdapter = new BoardCustomAdapter(getActivity(), descriptionList, upvoteList, locationList, statusList, imageList, authorList);

                if(boardList.getAdapter() == null){
                    boardList.setAdapter(customAdapter);
                }
                else{
                    ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when database is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        boardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent goToNavigationActivity = new Intent(getContext(), NavigationActivity.class);
                goToNavigationActivity.putExtra("latitude", String.valueOf(customAdapter.getLocation(position).getLatitude()));
                goToNavigationActivity.putExtra("longitude", String.valueOf(customAdapter.getLocation(position).getLongitude()));
                startActivity(goToNavigationActivity);
            }
        });
    }
}