package com.example.nasib.fixit;

/**
 * Created by nasib on 28-09-2017.
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.firebase.database.ChildEventListener;
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
import java.util.zip.Inflater;

public class BoardActivity extends Fragment implements AbsListView.OnScrollListener {
    ListView boardList;
    BoardCustomAdapter customAdapter;

    List<String> descriptionList;
    List<String> upvoteList;
    List<String> statusList;
    List<Boolean> imageList;
    List<String> authorList;
    List<Boolean> myLikes;

    SharedPreferences prefs;

    private DatabaseReference database;
    Query postsQuery;

    private int NUMBER_OF_POSTS_TO_SHOW_AT_A_TIME = 5;
    private int currentShownPosts = 0;
    private int preLast;
    private int index = 0;
    private String keyToStartAt = "";
    private int numberOfReportedPosts = 0;

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
        statusList = new ArrayList<>();
        imageList = new ArrayList<>();
        authorList = new ArrayList<>();
        myLikes = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();
        postsQuery = database.child("posts").orderByKey();
        prefs = getActivity().getApplicationContext().getSharedPreferences("Fixit_Preferences", Context.MODE_PRIVATE);

        boardList = (ListView) view.findViewById(R.id.boardListView); //creates a simple list with the layout of fragment_board.xml
        boardList.setOnScrollListener(this);

        currentShownPosts = NUMBER_OF_POSTS_TO_SHOW_AT_A_TIME;
        getFirstFivePosts();
    }

    private void getFirstFivePosts(){
        postsQuery.limitToLast(currentShownPosts + 1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot post : dataSnapshot.getChildren()){
                    if(index <= currentShownPosts){ //we don't actually insert the 6th element in the array, we just want the key
                        if(post.child("status").getValue().toString().equals("Pending") || post.child("status").getValue().toString().equals("Approved")){ //only display posts on the board that are marked as pending or approved
                            descriptionList.add(0, post.child("description").getValue().toString());
                            upvoteList.add(0, post.child("upvotes").getChildrenCount() + "");
                            statusList.add(0, post.child("status").getValue().toString());
                            imageList.add(0, Boolean.valueOf(post.child("image").getValue().toString()));
                            authorList.add(0, post.child("author").getValue().toString());

                            if(prefs.getString("username", null) == null){ //if user opens app for the first time, there is no shared prefs yet.
                                myLikes.add(0, false);
                            }
                            else{
                                if(post.child("upvotes").hasChild(prefs.getString("username", null))){
                                    myLikes.add(0, true);
                                }
                                else
                                {
                                    myLikes.add(0, false);
                                }
                            }
                        }
                        else
                        {
                            numberOfReportedPosts++;
                        }

                        if(keyToStartAt.equals("")){
                            keyToStartAt = post.getKey();
                        }
                        index++;
                    }
                }

                descriptionList.remove(currentShownPosts - numberOfReportedPosts);
                upvoteList.remove(currentShownPosts - numberOfReportedPosts);
                statusList.remove(currentShownPosts - numberOfReportedPosts);
                imageList.remove(currentShownPosts - numberOfReportedPosts);
                authorList.remove(currentShownPosts - numberOfReportedPosts);
                myLikes.remove(currentShownPosts - numberOfReportedPosts);

                customAdapter = new BoardCustomAdapter(getContext(), descriptionList, upvoteList, statusList, imageList, authorList, myLikes);

                if(boardList.getAdapter() == null){
                    boardList.setAdapter(customAdapter);
                }
                else{
                    ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when listview is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getFiveMorePosts(){
        final int currentSize = descriptionList.size();
        
        postsQuery.limitToLast(currentShownPosts - NUMBER_OF_POSTS_TO_SHOW_AT_A_TIME).endAt(keyToStartAt).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot post : dataSnapshot.getChildren()){
                    if(currentShownPosts - NUMBER_OF_POSTS_TO_SHOW_AT_A_TIME <= dataSnapshot.getChildrenCount()){
                        if(index <= currentShownPosts) { //we don't actually insert the 6th element in the array, we just want the key
                            if (post.child("status").getValue().toString().equals("Pending") || post.child("status").getValue().toString().equals("Approved")) { //only display posts on the board that are marked as pending or approved
                                descriptionList.add(currentSize, post.child("description").getValue().toString());
                                upvoteList.add(currentSize, post.child("upvotes").getChildrenCount() + "");
                                statusList.add(currentSize, post.child("status").getValue().toString());
                                imageList.add(currentSize, Boolean.valueOf(post.child("image").getValue().toString()));
                                authorList.add(currentSize, post.child("author").getValue().toString());


                                if (prefs.getString("username", null) == null) { //if user opens app for the first time, there is no shared prefs yet.
                                    myLikes.add(false);
                                } else {
                                    if (post.child("upvotes").hasChild(prefs.getString("username", null))) {
                                        myLikes.add(true);
                                    } else {
                                        myLikes.add(false);
                                    }
                                }
                            }
                            if(index == currentShownPosts + 1){
                                keyToStartAt = post.getKey();
                            }
                            index++;
                        }
                    }
                }

                ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when listview is updated
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateData(){
        /*
        postsQuery.limitToLast(posts_to_show).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                descriptionList.clear();
                upvoteList.clear();
                statusList.clear();
                imageList.clear();
                authorList.clear();
                myLikes.clear();

                for(DataSnapshot post : dataSnapshot.getChildren()){
                    if(post.child("status").getValue().toString().equals("Pending") || post.child("status").getValue().toString().equals("Approved")){ //only display posts on the board that are marked as pending or approved
                        descriptionList.add(post.child("description").getValue().toString());
                        upvoteList.add(post.child("upvotes").getChildrenCount() + "");
                        statusList.add(post.child("status").getValue().toString());
                        imageList.add(Boolean.valueOf(post.child("image").getValue().toString()));
                        authorList.add(post.child("author").getValue().toString());

                        if(prefs.getString("username", null) == null){ //if user opens app for the first time, there is no shared prefs yet.
                            myLikes.add(false);
                        }
                        else{
                            if(post.child("upvotes").hasChild(prefs.getString("username", null))){
                                myLikes.add(true);
                            }
                            else
                            {
                                myLikes.add(false);
                            }
                        }
                    }
                }

                //reverse list in order to display newest post at the top
                Collections.reverse(descriptionList);
                Collections.reverse(upvoteList);
                Collections.reverse(statusList);
                Collections.reverse(imageList);
                Collections.reverse(authorList);
                Collections.reverse(myLikes);

                customAdapter = new BoardCustomAdapter(getContext(), descriptionList, upvoteList, statusList, imageList, authorList, myLikes);

                if(boardList.getAdapter() == null){
                    boardList.setAdapter(customAdapter);
                }
                else{
                    ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when database is updated
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { //when use scrolls to bottom of listview, more posts are loaded
        if(view.getId() == R.id.boardListView){
            final int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount)
            {
                if(preLast!=lastItem) //to avoid multiple calls for last item
                {
                    currentShownPosts = currentShownPosts + NUMBER_OF_POSTS_TO_SHOW_AT_A_TIME;
                    getFiveMorePosts();
                    preLast = lastItem;
                }
            }
        }
    }
}