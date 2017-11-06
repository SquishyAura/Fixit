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

public class RewardActivity extends Fragment {
    ListView rewardList;
    RewardCustomAdapter rewardCustomAdapter;

    List<String> nameList;
    List<Integer> priceList;
    List<String> imageList;

    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reward, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nameList = new ArrayList<>();
        priceList = new ArrayList<>();
        imageList = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        rewardList = (ListView) view.findViewById(R.id.rewardListView); //creates a simple list with the layout of fragment_reward.xml

        database.child("rewards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameList.clear();
                priceList.clear();
                imageList.clear();

                for(DataSnapshot reward : dataSnapshot.getChildren()){
                    nameList.add(reward.child("name").getValue().toString());
                    priceList.add(Integer.parseInt(reward.child("price").getValue().toString()));
                    imageList.add(reward.child("image").getValue().toString());
                }

                Collections.reverse(nameList);
                Collections.reverse(priceList);
                Collections.reverse(imageList);

                rewardCustomAdapter = new RewardCustomAdapter(getContext(), nameList, priceList, imageList);

                if(rewardList.getAdapter() == null) {
                    rewardList.setAdapter(rewardCustomAdapter);
                }
                else {
                    ((RewardCustomAdapter)rewardList.getAdapter()).notifyDataSetChanged(); //prevent from scrolling to top when database is updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
