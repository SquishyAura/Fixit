package com.example.nasib.fixit;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DateTimeKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nasib.fixit.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    private DatabaseReference mDatabase;
    private SharedPreferences.Editor editor;
    String usernameString;
    Calendar calendar;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        usernameInput = (EditText) findViewById(R.id.usernameInput);

        //Shared preference for saving the username in the phone
        SharedPreferences prefs = getSharedPreferences("Fixit_Preferences",MODE_PRIVATE);
        editor = prefs.edit();

        //To read or write data from the database, you need an instance of DatabaseReference
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    public void btnLoginOnClick(View view) {
        usernameString = usernameInput.getText().toString().trim();

        if(usernameString.matches("[a-zA-Z0-9]{4,30}")){
            usernameString = usernameString.toLowerCase();
            usernameString = usernameString.substring(0,1).toUpperCase()+usernameString.substring(1);

            calendar = Calendar.getInstance();

            //Subtract with 7 because of our reward system
            calendar.add(Calendar.DATE,-7);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.format(calendar.getTime());

            //Use addListenerForSingleValueEvent to trigger once to check if the username already exists,
            //once the btnLoginOnClick is pressed
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(usernameString)){
                        //If name already exists, error
                        Toast errorToast = Toast.makeText(getApplicationContext(),R.string.toast_error_usernamealreadyexists, Toast.LENGTH_LONG);
                        errorToast.show();
                    }else{
                        newUser(usernameString, 0, 0, date, false);

                        //Save shared preference
                        editor.putString("username", usernameString);
                        editor.apply();

                        Toast successToast = Toast.makeText(getApplicationContext(),R.string.toast_success, Toast.LENGTH_SHORT);
                        successToast.show();

                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }else if(usernameString.length() < 4 || usernameString.length() > 30){
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.toast_error_usernamelength, Toast.LENGTH_LONG);
            errorToast.show();
        }else{
            Toast errorToast = Toast.makeText(getApplicationContext(),R.string.toast_error_usernamespecialchar, Toast.LENGTH_LONG);
            errorToast.show();
        }
    }

    private void newUser(String username, int points, int upvotes, String lastPurchase, boolean admin){
        User user = new User(username, points, upvotes, lastPurchase, admin);

        mDatabase.child("users").child(username).setValue(user);
    }
}
