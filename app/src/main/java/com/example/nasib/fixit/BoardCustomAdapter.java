package com.example.nasib.fixit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nasib.fixit.Entities.User;
import com.example.nasib.fixit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doalf on 10-10-2017.
 */

public class BoardCustomAdapter extends BaseAdapter {
    Context context;
    List<String> descriptionList;
    List<Integer> upvoteList;
    List<String> statusList;
    List<String> locationList;
    //List<String> imageList;
    List<String> authorList;
    LayoutInflater inflater;

    public BoardCustomAdapter(Context applicationContext, List<String> descriptionList, List<Integer> upvoteList, List<String> locationList, List<String> statusList, /*List<String> imageList,*/ List<String> authorList) {
        this.context = context;
        this.descriptionList = descriptionList;
        this.upvoteList = upvoteList;
        this.locationList = locationList;
        this.statusList = statusList;
        //this.imageList = imageList;
        this.authorList = authorList;
        inflater = (LayoutInflater.from(applicationContext));
    }

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
        TextView locationListTextView = (TextView) convertView.findViewById(R.id.boardLocation);
        TextView statusListTextView = (TextView) convertView.findViewById(R.id.boardStatus);
        //ImageView imageView = (ImageView) convertView.findViewById(R.id.boardImage);
        TextView authorListTextView = (TextView) convertView.findViewById(R.id.boardAuthor);

        descriptionListTextView.setText(descriptionList.get(position));
        upvoteListTextView.setText(upvoteList.get(position) + ""); // + "" in order to convert to string
        locationListTextView.setText(locationList.get(position));
        statusListTextView.setText(statusList.get(position));
        //imageView.setImageResource(imageList[position]);
        authorListTextView.setText(authorList.get(position));

        return convertView; //returns a row with all the textviews & images above
    }
}