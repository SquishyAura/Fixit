package com.example.nasib.fixit;

import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.Post;
import com.example.nasib.fixit.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreatePostActivity extends AppCompatActivity {
    private DatabaseReference database;
    Post post;
    EditText descriptionInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        getSupportActionBar().setTitle(R.string.create_post_screen_title);
        prefs = getSharedPreferences("Fixit_Preferences",MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        descriptionInput = (EditText) findViewById(R.id.descriptionEditText);
    }

    public void btnSubmitPostOnClick(View view) {
        if(descriptionInput.getText().toString().trim().length() < 10 || descriptionInput.getText().toString().trim().length() > 200){
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.create_post_failed_too_shortlong, Toast.LENGTH_LONG);
            errorToast.show();
        }
        else
        {
            post = new Post(descriptionInput.getText().toString().trim(), 0, new Location("rawr"), "Pending", "ugabugaimage", prefs.getString("username", null));

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
}
