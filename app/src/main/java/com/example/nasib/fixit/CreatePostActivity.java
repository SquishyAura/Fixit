package com.example.nasib.fixit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        getSupportActionBar().setTitle(R.string.create_post_screen_title);
        bindListeners();
    }

    private void bindListeners(){
        final Button cancelBtn = (Button) findViewById(R.id.cancelButtonCreatePost);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelActivity(view);
            }
        });
    }

    private void cancelActivity(View view){
        super.finish();
    }
}
