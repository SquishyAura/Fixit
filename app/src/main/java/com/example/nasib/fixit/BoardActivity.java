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

    private int posts_to_show = 5;
    private int preLast;

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

        loadData();
    }

    private void loadData(){
        postsQuery.limitToLast(posts_to_show).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {}
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
                if(preLast!=lastItem)
                {
                    //to avoid multiple calls for last item
                    posts_to_show = posts_to_show + 5;
                    loadData();
                    preLast = lastItem;
                }
            }
        }

    }
}