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

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

public class BoardActivity extends Fragment implements AbsListView.OnScrollListener {
    ListView boardList;

    List<String> descriptionList = new ArrayList<>();
    List<String> upvoteList = new ArrayList<>();
    List<String> statusList = new ArrayList<>();
    List<Boolean> imageList = new ArrayList<>();
    List<String> authorList = new ArrayList<>();
    List<Boolean> myLikes = new ArrayList<>();
    List<String> pushIDs = new ArrayList<>();

    BoardCustomAdapter customAdapter;

    SharedPreferences prefs;

    private DatabaseReference database;
    Query postsQuery;

    private int POSTS_TO_SHOW_AT_A_TIME = 10;
    private int preLast;
    private int index = 0;
    private String keyToStartAt = "";
    public boolean noPosts = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
        prefs = getActivity().getApplicationContext().getSharedPreferences("Fixit_Preferences", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        postsQuery = database.child("posts").orderByKey();
        customAdapter = new BoardCustomAdapter(getContext(), descriptionList, upvoteList, statusList, imageList, authorList, myLikes, pushIDs);
        boardList = (ListView) rootView.findViewById(R.id.boardListView); //creates a simple list with the layout of fragment_board.xml
        boardList.setOnScrollListener(this);

        //getAllPosts();
        updateData();
        return rootView;
    }

    private void  getAllPosts(){
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot post : dataSnapshot.getChildren()) {
                        descriptionList.add(0, post.child("description").getValue().toString());
                        upvoteList.add(0, post.child("upvotes").getChildrenCount() + "");
                        statusList.add(0, post.child("status").getValue().toString());
                        imageList.add(0, Boolean.valueOf(post.child("image").getValue().toString()));
                        authorList.add(0, post.child("author").getValue().toString());
                        pushIDs.add(0, post.getKey());

                        if (prefs.getString("username", null) == null) { //if user opens app for the first time, there is no shared prefs yet.
                            myLikes.add(0, false);
                        } else {
                            if (post.child("upvotes").hasChild(prefs.getString("username", null))) {
                                myLikes.add(0, true);
                            } else {
                                myLikes.add(0, false);
                            }
                        }
                    }

                    customAdapter = new BoardCustomAdapter(getContext(), descriptionList, upvoteList, statusList,
                                                           imageList, authorList, myLikes, pushIDs);
                    boardList.setAdapter(customAdapter);
                    ((BoardCustomAdapter) boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when listview is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getFirstFivePosts(){
        index = 0;
        postsQuery.limitToLast(POSTS_TO_SHOW_AT_A_TIME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot post : dataSnapshot.getChildren()) {
                        if (index < POSTS_TO_SHOW_AT_A_TIME) { //we don't actually insert the 6th element in the array, we just want the key
                            descriptionList.add(0, post.child("description").getValue().toString());
                            upvoteList.add(0, post.child("upvotes").getChildrenCount() + "");
                            statusList.add(0, post.child("status").getValue().toString());
                            imageList.add(0, Boolean.valueOf(post.child("image").getValue().toString()));
                            authorList.add(0, post.child("author").getValue().toString());
                            pushIDs.add(0, post.getKey());

                            if (prefs.getString("username", null) == null) { //if user opens app for the first time, there is no shared prefs yet.
                                myLikes.add(0, false);
                            } else {
                                if (post.child("upvotes").hasChild(prefs.getString("username", null))) {
                                    myLikes.add(0, true);
                                } else {
                                    myLikes.add(0, false);
                                }
                            }

                            if (index == 0) {
                                keyToStartAt = post.getKey();
                            }

                            index++;
                        }
                    }

                    customAdapter = new BoardCustomAdapter(getContext(), descriptionList, upvoteList, statusList, imageList, authorList, myLikes, pushIDs);
                    boardList.setAdapter(customAdapter);
                    ((BoardCustomAdapter) boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when listview is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getFiveMorePosts(){
        index = 0;
        final int currentSize = descriptionList.size();
        postsQuery.limitToLast(POSTS_TO_SHOW_AT_A_TIME + 1).endAt(keyToStartAt).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot post : dataSnapshot.getChildren()){
                        if(index <= dataSnapshot.getChildrenCount()) { //we don't actually insert the 6th element in the array, we just want the key
                            descriptionList.add(currentSize, post.child("description").getValue().toString());
                            upvoteList.add(currentSize, post.child("upvotes").getChildrenCount() + "");
                            statusList.add(currentSize, post.child("status").getValue().toString());
                            imageList.add(currentSize, Boolean.valueOf(post.child("image").getValue().toString()));
                            authorList.add(currentSize, post.child("author").getValue().toString());
                            pushIDs.add(currentSize, post.getKey());

                            if (prefs.getString("username", null) == null) { //if user opens app for the first time, there is no shared prefs yet.
                                myLikes.add(currentSize, false);
                            } else {
                                if (post.child("upvotes").hasChild(prefs.getString("username", null))) {
                                    myLikes.add(currentSize, true);
                                } else {
                                    myLikes.add(currentSize, false);
                                }
                            }

                            if(index == 0){
                                keyToStartAt = post.getKey();
                            }

                            index++;
                        }
                    }

                    descriptionList.remove(currentSize - 1);
                    upvoteList.remove(currentSize - 1);
                    statusList.remove(currentSize - 1);
                    imageList.remove(currentSize - 1);
                    authorList.remove(currentSize - 1);
                    myLikes.remove(currentSize - 1);
                    pushIDs.remove(currentSize - 1);
                    index--;

                    ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when listview is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateData(){
        database.child("posts").orderByKey().addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!pushIDs.isEmpty()){ //if arrays aren't empty, allow for new post.
                    descriptionList.add(0, dataSnapshot.child("description").getValue().toString());
                    upvoteList.add(0, dataSnapshot.child("upvotes").getChildrenCount() + "");
                    statusList.add(0, dataSnapshot.child("status").getValue().toString());
                    imageList.add(0, Boolean.valueOf(dataSnapshot.child("image").getValue().toString()));
                    authorList.add(0, dataSnapshot.child("author").getValue().toString());
                    myLikes.add(0, dataSnapshot.child("upvotes").hasChild(prefs.getString("username", null)));
                    pushIDs.add(0, dataSnapshot.getKey());

                    ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged();
                }
                else //else if arrays are empty, fetch up to 5 items from the database.
                {
                    if(noPosts == true){ //only allow 1 batch of posts to be loaded, as onChildAdded usually fires the amount of times there are posts in the database... in other words many times.
                        getFirstFivePosts();
                        noPosts = false;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for(int i = 0; i < pushIDs.size(); i++){
                    if(pushIDs.get(i).equals(dataSnapshot.getKey())){
                        descriptionList.set(i, dataSnapshot.child("description").getValue().toString());
                        upvoteList.set(i, dataSnapshot.child("upvotes").getChildrenCount() + "");
                        statusList.set(i, dataSnapshot.child("status").getValue().toString());
                        imageList.set(i, Boolean.valueOf(dataSnapshot.child("image").getValue().toString()));
                        authorList.set(i, dataSnapshot.child("author").getValue().toString());
                        myLikes.set(i, dataSnapshot.child("upvotes").hasChild(prefs.getString("username", null)));

                        ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(int i = 0; i < pushIDs.size(); i++){
                    if(pushIDs.get(i).equals(dataSnapshot.getKey())){
                        descriptionList.remove(i);
                        upvoteList.remove(i);
                        statusList.remove(i);
                        imageList.remove(i);
                        authorList.remove(i);
                        myLikes.remove(i);
                        pushIDs.remove(i);

                        ((BoardCustomAdapter)boardList.getAdapter()).notifyDataSetChanged();
                    }
                }

                if(pushIDs.isEmpty()){ //if there are no posts left in the database
                    noPosts = true;
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    getFiveMorePosts();
                    preLast = lastItem;
                }
            }
        }
    }
}