package com.example.nasib.fixit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BoardActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        prefs = getSharedPreferences("Fixit_Preferences", MODE_PRIVATE);

        if(!prefs.contains("username")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }


    }
}
