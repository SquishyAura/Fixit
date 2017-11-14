package com.example.nasib.fixit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Reward;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.namespace.QName;

/**
 * Created by doalf on 20-10-2017.
 */

public class CreateRewardActivity extends AppCompatActivity {
    private DatabaseReference database;
    EditText nameInput;
    EditText priceInput;
    ImageView thumbnail;
    LinearLayout linearLayout;
    String imageEncodedInBase64;
    private String noImageFound = "";
    Reward reward;

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int PICK_FROM_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reward);
        getSupportActionBar().setTitle(R.string.create_reward_screen_title);
        database = FirebaseDatabase.getInstance().getReference();

        nameInput = (EditText) findViewById(R.id.rewardNameEditText);
        priceInput = (EditText) findViewById(R.id.rewardPriceEditText);
        thumbnail = (ImageView) findViewById(R.id.rewardImageThumbnail);
        linearLayout = (LinearLayout) findViewById(R.id.rewardImageThumbnailContainer);
    }


    public void btnTakeRewardPictureOnClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //reduces image size, so that we don't get out of memory exception
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            //a bitmap is created to decode the InputStream
            Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream, null, options);

            //Write a compressed version of the bitmap to the ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] byteArray = baos.toByteArray();

            //The bytearray is lastly encoded to a base64 string, which is used to store images as strings in the database.
            imageEncodedInBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            //Thumbnail
            byte[] decodedString = Base64.decode(imageEncodedInBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            thumbnail.setImageBitmap(decodedByte);
            thumbnail.setMaxWidth(decodedByte.getWidth() / 2);
            thumbnail.setMaxHeight(decodedByte.getHeight() / 2);
            linearLayout.setBackgroundColor(Color.BLACK); //black outline for thumbnail
        }
    }

    public void btnSubmitRewardOnClick(View view) {
        if(nameInput.getText().toString().trim().length() < 4 || nameInput.getText().toString().trim().length() > 100){
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.create_reward_failed_too_shortlong, Toast.LENGTH_LONG);
            errorToast.show();
        }
        else if(!priceInput.getText().toString().trim().matches("[0-9]{1,5}")){
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.create_reward_price_not_integer, Toast.LENGTH_LONG);
            errorToast.show();
        }
        else
        {
            if(imageEncodedInBase64 != null) { //if an image is captured by the camera, that image is encoded into base64 and sent to the database.
                reward = new Reward(nameInput.getText().toString().trim(), Integer.valueOf(priceInput.getText().toString().trim()), imageEncodedInBase64);
            }
            else //if no image is captured, then a premade base64 image is displayed instead, which says "NO IMAGE AVAILABLE"
            {
                reward = new Reward(nameInput.getText().toString().trim(), Integer.valueOf(priceInput.getText().toString().trim()), noImageFound);
            }

            //send a single event to the database, which pushes the post we just created
            database.child("rewards").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    database.child("rewards").push().setValue(reward); //we use push() to create a unique id.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Toast successToast = Toast.makeText(getApplicationContext(),R.string.create_reward_successful, Toast.LENGTH_SHORT);
            successToast.show();
            super.finish();
        }
    }

    public void btnCancelRewardOnClick(View view) {
        super.finish();
    }
}