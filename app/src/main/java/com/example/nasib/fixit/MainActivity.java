package com.example.nasib.fixit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Position;
import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

    //Permission
    private static final int REQUEST_CODE = 2;

    //Post thresholds
    private static final int POST_APPROVAL_THRESHOLD = 5;
    private static final int POST_DUPLICATE_THRESHOLD = 5;
    private static final int POST_REPORT_THRESHOLD = 5;
    private static final int REWARD_COOLDOWN_DURATION_DAYS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FIXIT");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        toolbar.setSubtitleTextColor(Color.parseColor("#ffffff"));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //set fragment TAG
        //getSupportFragmentManager().beginTransaction().replace(R.id.container, new BoardActivity(), "SOMETAG").commit();
        //get fragment TAG
        //FragmentManager fm = getSupportFragmentManager();
        //BoardActivity boardActivity = (BoardActivity) fm.findFragmentByTag("SOMETAG");
        //boardActivity.updateUpvote(dataSnapshot);

        prefs = getSharedPreferences("Fixit_Preferences", MODE_PRIVATE);
        editor = prefs.edit();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fabCreatePost = (FloatingActionButton) findViewById(R.id.fabCreatePost);
        fabCreateReward = (FloatingActionButton) findViewById(R.id.fabCreateReward);
        fabCreatePost.show();
        fabCreateReward.hide();

        //Set the current tab.
        setDefaultTab(1);

        //PERMISSIONS FOR LOCATION & STORAGE
        requestPermissions();

        //Check if user is registered, by checking the shared preferences file. If not then send to login page.
        if (!prefs.contains("username")) {
            sendUserToLoginActivity();
        } else //else if user is registered in shared prefs but not in the database for some reason, remove shared preference and move to login activity.
        {
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(prefs.getString("username", null))) {
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
                if (prefs.contains("username")) {
                    if (!dataSnapshot.hasChild(prefs.getString("username", null)) && !prefs.getString("username", null).equals(null)) { //if database doesn't contain username and shared prefs contain username (this occurs when we delete a user through the database directly)
                        editor.clear().commit(); //remove everything in shared preferences
                        sendUserToLoginActivity();
                    } else {
                        //Displays user's username & points at the top bar
                        displayMyInfo();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Listener listens when the user changes tabs. We use this in order to dynamically hide & show certain fabs.
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mViewPager.getCurrentItem() == 0) { //if current tab is the reward tab
                    mDatabase.child("users").child(prefs.getString("username", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (Boolean.valueOf(dataSnapshot.child("admin").getValue().toString()) == true) {
                                fabCreateReward.show();
                            } else {
                                fabCreateReward.hide();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    fabCreatePost.hide();
                }

                if (mViewPager.getCurrentItem() == 1) { //if current tab is the board tab
                    fabCreatePost.show();
                    fabCreateReward.hide();
                }

                if (mViewPager.getCurrentItem() == 2) { //if current tab is the highscore tab
                    fabCreatePost.hide();
                    fabCreateReward.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void setDefaultTab(int tabNumber) {
        mViewPager.setCurrentItem(tabNumber, false); //default tab is board tab
    }

    public void sendUserToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void btnCreatePostOnClick(View view) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }

    public void btnCreateRewardOnClick(View view) {
        Intent intent = new Intent(this, CreateRewardActivity.class);
        startActivity(intent);
    }

    public void displayMyInfo(){
        mDatabase.child("users").child(prefs.getString("username", null)).child("points").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setSubtitle(prefs.getString("username", null) + " - " + dataSnapshot.getValue().toString() + " points");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void btnDisplayImageOnClick(final View view) {
        mDatabase.child("posts").child(String.valueOf(view.getTag())).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (Boolean.valueOf(dataSnapshot.child("image").getValue().toString()) == false) { //if user has not upvoted the post yet, allow upvote
                    Toast.makeText(getApplicationContext(), R.string.post_display_no_image, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
                    intent.putExtra("imageBase64", String.valueOf(view.getTag()));
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void btnDisplayMapOnClick(View view) {
        mDatabase.child("posts").child(String.valueOf(view.getTag())).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent goToNavigationActivity = new Intent(getApplicationContext(), NavigationActivity.class);
                goToNavigationActivity.putExtra("latitude", String.valueOf(dataSnapshot.child("position").child("latitude").getValue().toString()));
                goToNavigationActivity.putExtra("longitude", String.valueOf(dataSnapshot.child("position").child("longitude").getValue().toString()));
                startActivity(goToNavigationActivity);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void btnBuyRewardOnClick(final View view) {
        mDatabase.child("rewards").child(String.valueOf(view.getTag())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Calendar c = Calendar.getInstance();
                    Date dt = new Date(); //get current date
                    c.setTime(dt);
                    c.add(Calendar.DATE, REWARD_COOLDOWN_DURATION_DAYS); //add 7 days to date, in order to make cooldown last 7 days
                    dt = c.getTime();
                    String futureTimeStamp = new SimpleDateFormat("dd/MM-yyyy HH:mm:ss").format(dt);
                    String currentTimeStamp = new SimpleDateFormat("dd/MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());

                    if(dataSnapshot.child("cooldown").hasChild(prefs.getString("username", null))){ //if user has cooldown
                        if(dataSnapshot.child("cooldown").child(prefs.getString("username", null)).getValue().toString().compareTo(currentTimeStamp) <= 0){ //if user is not on buy cooldown for the certain reward
                            deductUserPoint(prefs.getString("username", null), String.valueOf(view.getTag()), Integer.valueOf(dataSnapshot.child("price").getValue().toString()), futureTimeStamp);
                        }
                        else{
                            String cooldownStr = getString(R.string.reward_buy_cooldown, dataSnapshot.child("cooldown").child(prefs.getString("username", null)).getValue().toString());
                            Toast.makeText(getApplicationContext(), cooldownStr, Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        deductUserPoint(prefs.getString("username", null), String.valueOf(view.getTag()), Integer.valueOf(dataSnapshot.child("price").getValue().toString()), futureTimeStamp);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //upvote / remove upvote button
    public void btnUpvotePostOnClick(final View view) {
        mDatabase.child("posts").child(String.valueOf(view.getTag())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (!dataSnapshot.child("upvotes").hasChild(prefs.getString("username", null))) { //if user has not upvoted the post yet, allow upvote
                        if (!dataSnapshot.child("author").getValue().toString().equals(prefs.getString("username", null))) { //you cannot like your own post!
                            incrementPostUpovote(String.valueOf(view.getTag()));
                            incrementUserUpvote(dataSnapshot.child("author").getValue().toString(), String.valueOf(view.getTag()), dataSnapshot.child("status").getValue().toString());
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.upvote_own_post_not_allowed, Toast.LENGTH_SHORT).show();
                        }
                    } else //else if user has upvoted already, remove upvote
                    {
                        decrementPostUpvote(String.valueOf(view.getTag()));
                        decrementUserUpvote(dataSnapshot.child("author").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementPostUpovote(final String postKey){
        mDatabase.child("posts").child(postKey).child("upvotes").child(prefs.getString("username", null)).setValue(prefs.getString("username", null));

        mDatabase.child("posts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("upvotes").getChildrenCount() >= POST_APPROVAL_THRESHOLD && //if post has at least 5 upvotes
                   !dataSnapshot.child("status").getValue().toString().equals("Approved")){  //and if post hasn't been approved yet
                    incrementUserPoint(dataSnapshot.child("author").getValue().toString()); //increment point of post's author
                    approvePost(postKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void decrementPostUpvote(final String postKey){
        mDatabase.child("posts").child(postKey).child("upvotes").child(prefs.getString("username", null)).removeValue();
    }

    public void incrementUserUpvote(final String postAuthor, final String postKey, final String postStatus) {
        mDatabase.child("users").child(postAuthor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if post author exists in the database
                    int incrementedUpvote = Integer.valueOf(dataSnapshot.child("upvotes").getValue().toString()) + 1;
                    mDatabase.child("users").child(postAuthor).child("upvotes").setValue(incrementedUpvote);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void decrementUserUpvote(final String postAuthor) {
        mDatabase.child("users").child(postAuthor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if post author exists in the database
                    int decrementedUpvote = Integer.valueOf(dataSnapshot.child("upvotes").getValue().toString()) - 1;
                    mDatabase.child("users").child(postAuthor).child("upvotes").setValue(decrementedUpvote);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementUserPoint(final String postAuthor){
        mDatabase.child("users").child(postAuthor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if post author exists in the database
                    int incrementedPoint = Integer.valueOf(dataSnapshot.child("points").getValue().toString()) + 1;
                    mDatabase.child("users").child(postAuthor).child("points").setValue(incrementedPoint);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deductUserPoint(final String username, final String rewardKey, final int rewardPrice, final String timeStamp){
        mDatabase.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if post author exists in the database
                    if(Integer.valueOf(dataSnapshot.child("points").getValue().toString()) >= rewardPrice){ //if user has enough points to buy reward
                        mDatabase.child("rewards").child(rewardKey).child("cooldown").child(prefs.getString("username", null)).setValue(timeStamp);

                        int remainingPoints = Integer.valueOf(dataSnapshot.child("points").getValue().toString()) - rewardPrice;
                        mDatabase.child("users").child(username).child("points").setValue(remainingPoints);

                        String successStr = getString(R.string.reward_buy_successful, String.valueOf(remainingPoints));
                        Toast.makeText(getApplicationContext(), successStr, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.reward_buy_not_enough_points, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void approvePost(final String postKey){
        mDatabase.child("posts").child(postKey).child("status").setValue("Approved"); //approve post
    }

    public void btnShowMoreOnClick(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_an_option);

        //radiobutton options
        CharSequence[] options = {getString(R.string.mark_as_duplicate), getString(R.string.report_this_post)};
        final String[] selectedOption = new String[1];
        builder.setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() { //-1 means no option is selected
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = String.valueOf(which);
            }
        });

        //ok button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });

        //cancel button
        builder.setPositiveButton(R.string.send_in, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        //we have this code in order to prevent the ok button from closing once it's being pressed.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if(wantToCloseDialog) {
                    dialog.dismiss();
                }
                else{
                    if(selectedOption[0] == null){
                        Toast.makeText(getApplicationContext(), R.string.choose_an_option, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(selectedOption[0].equals("0")){ //if duplicate option is selected
                            markPostAsDuplicate(String.valueOf(view.getTag()), dialog);
                        }
                        else if(selectedOption[0].equals("1")) //else if report option is selected
                        {
                            reportPost(String.valueOf(view.getTag()), dialog);
                        }

                    }
                }
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }

    public void markPostAsDuplicate(final String postKey, final AlertDialog dialog){
        mDatabase.child("posts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("duplicates").hasChild(prefs.getString("username", null))) { //if user has not marked the post as duplicate yet, allow marking
                    incrementDuplicate(postKey);
                    Toast.makeText(getApplicationContext(), R.string.mark_as_duplicate_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.mark_as_duplicate_fail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementDuplicate(final String postKey){
        mDatabase.child("posts").child(postKey).child("duplicates").child(prefs.getString("username", null)).setValue(prefs.getString("username", null));

        mDatabase.child("posts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("duplicates").getChildrenCount() >= POST_DUPLICATE_THRESHOLD && //if post has been marked at least 5 times as duplicate
                   !dataSnapshot.child("status").getValue().toString().equals("Approved")){  //and if post hasn't been approved yet
                    mDatabase.child("posts").child(postKey).child("status").setValue("Duplicated"); //mark post as duplicated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void reportPost(final String postKey, final AlertDialog dialog){
        mDatabase.child("posts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("reports").hasChild(prefs.getString("username", null))) { //if user has not reported the post yet, allow reporting
                    incrementReport(postKey);
                    Toast.makeText(getApplicationContext(), R.string.report_this_post_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.report_this_post_fail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void incrementReport(final String postKey){
        mDatabase.child("posts").child(postKey).child("reports").child(prefs.getString("username", null)).setValue(prefs.getString("username", null));

        mDatabase.child("posts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("reports").getChildrenCount() >= POST_REPORT_THRESHOLD && //if post has been reported at least 5 times
                   !dataSnapshot.child("status").getValue().toString().equals("Approved")){  //and if post hasn't been approved yet
                    mDatabase.child("posts").child(postKey).removeValue(); //remove reported post
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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
            switch (position) {
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

    public void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //if permissions weren't already granted, ask for permissions now (example: logging in for the first time on the app)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            for(int i = 0; i < grantResults.length; i++){ //check if EVERY SINGLE permission has been granted
                if (grantResults.length == 3 && grantResults[i] != PackageManager.PERMISSION_GRANTED) { //if not, close application
                    finish();
                }
            }
        }
    }
}
