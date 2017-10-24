package com.example.nasib.fixit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreatePostActivity extends AppCompatActivity {
    private DatabaseReference database;
    Post post;
    private String noImageFound = "";
    EditText descriptionInput;
    String imageEncodedInBase64;
    private SharedPreferences prefs;
    static final int REQUEST_OK_PHOTO = 1;
    static final int REQUEST_OK_LOCATION = 2;
    String mCurrentPhotoPath;
    File photoFile = null;
    Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        getSupportActionBar().setTitle(R.string.create_post_screen_title);
        prefs = getSharedPreferences("Fixit_Preferences",MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        descriptionInput = (EditText) findViewById(R.id.descriptionEditText);
    }

    public void btnTakePictureOnClick(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_OK_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OK_PHOTO && resultCode == RESULT_OK) {
            Uri uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //reduces image size, so that we don't get out of memory exception
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            //a bitmap is created to decode the InputStream
            Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream, null, options);

            //rotate image 90 degrees
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

            //Write a compressed version of the bitmap to the ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();

            //The bytearray is lastly encoded to a base64 string, which is used to store images as strings in the database.
            imageEncodedInBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        else if(requestCode == REQUEST_OK_LOCATION && resultCode == RESULT_OK){
            Bundle b = data.getExtras();
            location = (Location) b.get("Location");

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void btnSubmitPostOnClick(View view) {
        if(descriptionInput.getText().toString().trim().length() < 10 || descriptionInput.getText().toString().trim().length() > 200){
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.create_post_failed_too_shortlong, Toast.LENGTH_LONG);
            errorToast.show();
        }
        else if(location == null){ //if location hasn't been chosen
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.create_post_location_error, Toast.LENGTH_LONG);
            errorToast.show();
        }
        else
        {
            if(imageEncodedInBase64 != null){ //if an image is captured by the camera, that image is encoded into base64 and sent to the database.
                post = new Post(descriptionInput.getText().toString().trim(), "", location, "Pending",  imageEncodedInBase64, prefs.getString("username", null));
            }
            else //if no image is captured, then a premade base64 image is displayed instead, which says "NO IMAGE AVAILABLE"
            {
                post = new Post(descriptionInput.getText().toString().trim(), "", location, "Pending", noImageFound, prefs.getString("username", null));
            }


            //send a single event to the database, which pushes the post we just created
            database.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    database.child("posts").push().setValue(post); //we use push() to create a unique id.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast successToast = Toast.makeText(getApplicationContext(),R.string.create_post_successful, Toast.LENGTH_SHORT);
            successToast.show();
            super.finish();
        }
    }

    public void btnCancelPostOnClick(View view) {
        super.finish();
    }

    public void btnSetLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, REQUEST_OK_LOCATION);
    }


}
