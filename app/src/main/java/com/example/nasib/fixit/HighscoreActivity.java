package com.example.nasib.fixit;

/**
 * Created by nasib on 28-09-2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighscoreActivity extends Fragment {
    ListView highscoreList;
    HighscoreCustomAdapter customAdapter;

    List<String> nameList;
    List<Integer> scoreList;

    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_highscore, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nameList = new ArrayList<>();
        scoreList = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        highscoreList = (ListView) view.findViewById(R.id.highscoreListView); //creates a simple list with the layout of fragment_highscore.xml

        database.child("users").orderByChild("upvotes").limitToLast(10).addValueEventListener(new ValueEventListener() { //takes top 20 upvotes

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameList.clear();
                scoreList.clear();

                for(DataSnapshot users : dataSnapshot.getChildren()){
                    nameList.add(users.child("username").getValue().toString());
                    scoreList.add(Integer.parseInt(users.child("upvotes").getValue().toString()));
                }

                Collections.reverse(nameList);
                Collections.reverse(scoreList);

                customAdapter = new HighscoreCustomAdapter(getActivity(), nameList, scoreList);

                if(highscoreList.getAdapter() == null){
                    highscoreList.setAdapter(customAdapter);
                }
                else {
                    ((HighscoreCustomAdapter)highscoreList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when database is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
