package com.example.nasib.fixit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;
import java.util.List;

/**
 * Created by doalf on 10-10-2017.
 */

public class BoardCustomAdapter extends BaseAdapter {
    Context context;
    List<String> descriptionList;
    List<String> upvoteList;
    List<String> statusList;
    List<Location> locationList;
    List<String> imageList;
    List<String> authorList;
    List<Boolean> myLikes;
    LayoutInflater inflater;
    DatabaseReference mDatabase;

    public BoardCustomAdapter(Context applicationContext, List<String> descriptionList, List<String> upvoteList, List<Location> locationList, List<String> statusList, List<String> imageList, List<String> authorList, List<Boolean> myLikes) {
        this.context = applicationContext;
        this.descriptionList = descriptionList;
        this.upvoteList = upvoteList;
        this.locationList = locationList;
        this.statusList = statusList;
        this.imageList = imageList;
        this.authorList = authorList;
        this.myLikes = myLikes;
        inflater = (LayoutInflater.from(applicationContext));
    }

    public Location getLocation(int position) { return locationList.get(position); }

    @Override
    public int getCount() {
        return descriptionList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_board_list_view, null);

        TextView statusListTextView = (TextView) convertView.findViewById(R.id.boardStatus);
        TextView descriptionListTextView = (TextView) convertView.findViewById(R.id.boardDescriptionText);
        TextView authorListTextView = (TextView) convertView.findViewById(R.id.boardAuthor);
        Button upvoteBtn = (Button) convertView.findViewById(R.id.boardUpvoteButton);
        Button displayImage = (Button) convertView.findViewById(R.id.boardImageButton);
        Button displayMap = (Button) convertView.findViewById(R.id.boardMapButton);
        Button showMore = (Button) convertView.findViewById(R.id.boardShowMore);

        upvoteBtn.setTag(position); //set position to a button in order to track which button is being pressed.
        displayImage.setTag(position);
        displayMap.setTag(position);
        showMore.setTag(position);

        descriptionListTextView.setText(descriptionList.get(position));
        authorListTextView.setText("By: " + authorList.get(position));
        upvoteBtn.setText(upvoteList.get(position));

        alternateBackgroundColors(position, convertView);
        assignStatusColors(position, statusListTextView);
        assignUpvoteBtnColor(position, upvoteBtn);
        assignImageBtnColor(position, displayImage);

        return convertView; //returns a row with all the textviews, images, and buttons above
    }

    public void alternateBackgroundColors(int position, View convertView){
        if(position % 2 == 0){
            convertView.findViewById(R.id.boardPostContent).setBackgroundColor(Color.parseColor("#ededed"));
        }
        else{
            convertView.findViewById(R.id.boardPostContent).setBackgroundColor(Color.parseColor("#f7f7f7"));
        }
    }

    public void assignStatusColors(int position, TextView statusListTextView){
        if(statusList.get(position).equals("Pending")){
            statusListTextView.setBackgroundColor(Color.parseColor("#f2e237"));
        }
        else if(statusList.get(position).equals("Approved")){
            statusListTextView.setBackgroundColor(Color.parseColor("#48a999"));
        }
        else if(statusList.get(position).equals("Reported") || statusList.get(position).equals("Duplicated")){
            statusListTextView.setBackgroundColor(Color.parseColor("#e2a59c"));
        }
    }

    public void assignUpvoteBtnColor(int position, Button upvoteBtn){
        if(myLikes.get(position)){
            upvoteBtn.setBackgroundResource(R.mipmap.ic_thumb_up_green_24dp);
        }
        else
        {
            upvoteBtn.setBackgroundResource(R.mipmap.ic_thumb_up_grey_24dp);
        }
    }

    public void assignImageBtnColor(int position, Button displayImage){
        if(imageList.get(position).length() == 0){
            displayImage.setBackgroundResource(R.mipmap.ic_image_grey_24dp);
        }
        else
        {
            displayImage.setBackgroundResource(R.mipmap.ic_image_cyan_24dp);
        }
    }
}