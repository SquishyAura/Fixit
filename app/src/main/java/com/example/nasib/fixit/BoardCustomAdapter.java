package com.example.nasib.fixit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
    LayoutInflater inflater;

    public BoardCustomAdapter(Context applicationContext, List<String> descriptionList, List<String> upvoteList, List<Location> locationList, List<String> statusList, List<String> imageList, List<String> authorList) {
        this.context = applicationContext;
        this.descriptionList = descriptionList;
        this.upvoteList = upvoteList;
        this.locationList = locationList;
        this.statusList = statusList;
        this.imageList = imageList;
        this.authorList = authorList;
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

        TextView descriptionListTextView = (TextView) convertView.findViewById(R.id.boardDescriptionText);
        TextView upvoteListTextView = (TextView) convertView.findViewById(R.id.boardUpvoteText);
        Button upvoteBtn = (Button) convertView.findViewById(R.id.boardUpvoteButton);
        TextView statusListTextView = (TextView) convertView.findViewById(R.id.boardStatus);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.boardImage);
        TextView authorListTextView = (TextView) convertView.findViewById(R.id.boardAuthor);

        upvoteBtn.setTag(position); //set position to a button in order to track which button is being pressed.

        if(imageList.get(position).equals("")){ //if no image has been selected in the create post activity
            imageView.setImageResource(R.mipmap.no_image_available);
        }
        else //else if an image was selected and stored as a base64 string
        {
            byte[] decodedString = Base64.decode(imageList.get(position), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        }

        descriptionListTextView.setText(descriptionList.get(position));
        upvoteListTextView.setText("Upvotes: " + upvoteList.get(position));
        statusListTextView.setText("Status: " + statusList.get(position));
        authorListTextView.setText("Author: " + authorList.get(position));

        return convertView; //returns a row with all the textviews, images, and buttons above
    }
}