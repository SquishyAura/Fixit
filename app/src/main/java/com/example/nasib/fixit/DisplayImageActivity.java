package com.example.nasib.fixit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayImageActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        getSupportActionBar().hide();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final ImageView image = (ImageView) findViewById(R.id.displayFullImage);
        final String imageKey = getIntent().getStringExtra("imageBase64");

        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                byte[] decodedString = Base64.decode(dataSnapshot.child(imageKey).child("image").getValue().toString(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                image.setImageBitmap(decodedByte);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void btnGoBackFromImageOnClick(View view) {
        finish();
    }
}
