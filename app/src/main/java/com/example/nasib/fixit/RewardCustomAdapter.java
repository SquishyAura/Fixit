package com.example.nasib.fixit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * Created by doalf on 19-10-2017.
 */

public class RewardCustomAdapter extends BaseAdapter {
    Context context;
    List<String> nameList;
    List<Integer> priceList;
    List<String> imageList;
    LayoutInflater inflater;

    public RewardCustomAdapter(Context applicationContext, List<String> nameList, List<Integer> priceList, List<String> imageList) {
        this.context = applicationContext;
        this.nameList = nameList;
        this.priceList = priceList;
        this.imageList = imageList;
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
        convertView = inflater.inflate(R.layout.activity_reward_list_view, null);

        TextView nameListTextView = (TextView) convertView.findViewById(R.id.rewardNameText);
        TextView priceListTextView = (TextView) convertView.findViewById(R.id.rewardPriceText);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.rewardImage);
        Button buyBtn = (Button) convertView.findViewById(R.id.rewardBuyButton);

        nameListTextView.setText(nameList.get(position));
        priceListTextView.setText(priceList.get(position) + ""); // + "" in order to convert to string
        //image (selecting an image when creating a reward is REQUIRED, not optional.)
        byte[] decodedString = Base64.decode(imageList.get(position), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);

        buyBtn.setTag(position);

        return convertView;
    }
}
