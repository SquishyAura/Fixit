package com.example.nasib.fixit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private DatabaseReference mDatabase;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    FloatingActionButton fabCreatePost;
    FloatingActionButton fabCreateReward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        prefs = getSharedPreferences("Fixit_Preferences",MODE_PRIVATE);
        editor = prefs.edit();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fabCreatePost = (FloatingActionButton) findViewById(R.id.fabCreatePost);
        fabCreateReward = (FloatingActionButton) findViewById(R.id.fabCreateReward);

        //Check if user is registered, by checking the shared preferences file. If not then send to login page.
        if(!prefs.contains("username")){
            System.out.println("sender igennem 0");
            sendUserToLoginActivity();
        }
        else //else if user is registered in shared prefs but not in the database for some reason, remove shared preference and move to login activity.
        {
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(prefs.getString("username", null))){
                        editor.clear().commit();
                        sendUserToLoginActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Continously check database for username changes. If username gets deleted, remove from shared preference & send back to login page.
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(prefs.contains("username")){
                    if(!dataSnapshot.hasChild(prefs.getString("username", null)) && !prefs.getString("username", null).equals(null)){ //if database doesn't contain username and shared prefs contain username (this occurs when we delete a user through the database directly)
                        editor.clear().commit(); //remove everything in shared preferences
                        sendUserToLoginActivity();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Set the current tab.
        setDefaultTab(1);

        //Listener listens when the user changes tabs. We use this in order to dynamically hide & show certain fabs.
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(mViewPager.getCurrentItem() == 0){ //if current tab is the reward tab
                    mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot user : dataSnapshot.getChildren()){ //if current user is an admin, they can create a reward
                                if(Boolean.valueOf(user.child("admin").getValue().toString()) == true){
                                    fabCreateReward.show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    fabCreatePost.hide();
                }

                if(mViewPager.getCurrentItem() == 1){ //if current tab is the board tab
                    fabCreatePost.show();
                    fabCreateReward.hide();
                }

                if(mViewPager.getCurrentItem() == 2){ //if current tab is the highscore tab
                    fabCreatePost.hide();
                    fabCreateReward.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    public void setDefaultTab(int tabNumber){
        mViewPager.setCurrentItem(tabNumber, false); //default tab is board tab

        //since default tab is board tab, we want to hide reward fab and only display board fab.
        if(mViewPager.getCurrentItem() == 0){ //if current tab is the reward tab
            fabCreatePost.hide();
            fabCreateReward.show();
        }

        if(mViewPager.getCurrentItem() == 1){ //if current tab is the board tab
            fabCreatePost.show();
            fabCreateReward.hide();
        }

        if(mViewPager.getCurrentItem() == 2){ //if current tab is the highscore tab
            fabCreatePost.hide();
            fabCreateReward.hide();
        }
    }

    public void sendUserToLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void btnCreatePostOnClick(View view) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }

    //upvote / remove upvote button
    public void btnUpvotePostOnClick(final View view) {
        final int currentButtonPos = (int) view.getTag(); //we get button position from the listview, since every post in listview has a button.
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){ //if there are any posts
                    int i = (int) dataSnapshot.getChildrenCount() - 1; //-1 because an array starts at 0, not 1. Also, we start at array size and decrement because we're inversing the indexes, because posts are already inversed as they need to be displayed from top to bottom.

                    for(DataSnapshot posts : dataSnapshot.getChildren()){ //get all posts
                        if(i == currentButtonPos){ //since we want to add values to a CERTAIN post, we get the post index from the for-loop, and if that post index is the same as the button index then add upvote
                            if(!dataSnapshot.child(posts.getKey()).child("upvotes").hasChild(prefs.getString("username", null))){ //if user has not upvoted the post yet, allow upvote
                                if(!dataSnapshot.child(posts.getKey()).child("author").getValue().toString().equals(prefs.getString("username", null))){ //you cannot like your own post!
                                    mDatabase.child("posts").child(posts.getKey()).child("upvotes").child(prefs.getString("username", null)).setValue(prefs.getString("username", null));
                                    incrementUpvote(dataSnapshot.child(posts.getKey()).child("author").getValue().toString());
                                }
                                else
                                {
                                    Toast errorToast = Toast.makeText(getApplicationContext(),R.string.upvote_own_post_not_allowed, Toast.LENGTH_SHORT);
                                    errorToast.show();
                                }
                            }
                            else //else if user has upvoted already, remove upvote
                            {
                                mDatabase.child("posts").child(posts.getKey()).child("upvotes").child(prefs.getString("username", null)).removeValue();
                                decrementUpvote(dataSnapshot.child(posts.getKey()).child("author").getValue().toString());
                            }
                        }
                        i--;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementUpvote(final String postAuthor){
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(postAuthor)){ //if post author exists in the database
                    int currentUpvote = Integer.valueOf(dataSnapshot.child(postAuthor).child("upvotes").getValue().toString());
                    int incrementedUpvote = currentUpvote + 1;
                    mDatabase.child("users").child(postAuthor).child("upvotes").setValue(incrementedUpvote);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void decrementUpvote(final String postAuthor){
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(postAuthor)) { //if post author exists in the database
                    int currentUpvote = Integer.valueOf(dataSnapshot.child(postAuthor).child("upvotes").getValue().toString());
                    int decrementedUpvote = currentUpvote - 1;
                    mDatabase.child("users").child(postAuthor).child("upvotes").setValue(decrementedUpvote);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void btnCreateRewardOnClick(View view) {
        Intent intent = new Intent(this, CreateRewardActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    RewardActivity tab1 = new RewardActivity();
                    return tab1;
                case 1:
                    BoardActivity tab2 = new BoardActivity();
                    return tab2;
                case 2:
                    HighscoreActivity tab3 = new HighscoreActivity();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Rewards";
                case 1:
                    return "Board";
                case 2:
                    return "Highscore";
            }
            return null;
        }
    }
}
