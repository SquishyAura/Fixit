package com.example.nasib.fixit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

        TextView nameListTextView = (TextView) convertView.findViewById(R.id.highscoreNameText);
        TextView scoreListTextView = (TextView) convertView.findViewById(R.id.highscoreScoreText);

        nameListTextView.setText("#" + position + 1 + " - " + nameList.get(position));
        scoreListTextView.setText(scoreList.get(position) + " upvotes");

        return convertView;
    }
}
