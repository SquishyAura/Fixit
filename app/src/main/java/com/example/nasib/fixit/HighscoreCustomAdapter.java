package com.example.nasib.fixit;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by doalf on 25-10-2017.
 */

public class HighscoreCustomAdapter extends BaseAdapter {
    Context context;
    List<String> nameList;
    List<Integer> scoreList;
    LayoutInflater inflater;

    public HighscoreCustomAdapter(Context applicationContext, List<String> nameList, List<Integer> scoreList) {
        this.context = applicationContext;
        this.nameList = nameList;
        this.scoreList = scoreList;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return nameList.size();
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
        convertView = inflater.inflate(R.layout.activity_highscore_list_view, null);

        TextView rankTextView = (TextView) convertView.findViewById(R.id.highscoreRankText);
        TextView nameListTextView = (TextView) convertView.findViewById(R.id.highscoreNameText);
        TextView scoreListTextView = (TextView) convertView.findViewById(R.id.highscoreUpvoteText);
        ImageView upvoteImage = (ImageView) convertView.findViewById(R.id.highscoreUpvoteImage);

        int incrementedPosition = position + 1;
        rankTextView.setText("#" + incrementedPosition);
        nameListTextView.setText(nameList.get(position));
        scoreListTextView.setText(scoreList.get(position) + "");

        alternateBackgroundColors(position, convertView);
        topPositionsTextLayout(position, nameListTextView, scoreListTextView, upvoteImage);

        return convertView;
    }

    public void alternateBackgroundColors(int position, View convertView){
        if(position % 2 == 0){
            convertView.findViewById(R.id.highscoreContent).setBackgroundColor(Color.parseColor("#ededed"));
        }
        else{
            convertView.findViewById(R.id.highscoreContent).setBackgroundColor(Color.parseColor("#f7f7f7"));
        }

    }

    public void topPositionsTextLayout(int position, TextView nameList, TextView scoreList, ImageView upvoteImage){
        if(position == 0){
            nameList.setTextSize(40);
            scoreList.setTextSize(25);
            upvoteImage.getLayoutParams().height = 121;
            upvoteImage.getLayoutParams().width = 121;
            upvoteImage.requestLayout();
        }
        else if(position == 1){
            nameList.setTextSize(30);
            scoreList.setTextSize(20);
            upvoteImage.getLayoutParams().height = 97;
            upvoteImage.getLayoutParams().width = 97;
            upvoteImage.requestLayout();
        }
        else if(position == 2){
            nameList.setTextSize(20);
            scoreList.setTextSize(15);
            upvoteImage.getLayoutParams().height = 72;
            upvoteImage.getLayoutParams().width = 72;
            upvoteImage.requestLayout();
        }
        else
        {
            upvoteImage.getLayoutParams().height = 68;
            upvoteImage.getLayoutParams().width = 68;
            upvoteImage.requestLayout();
        }
    }
}
