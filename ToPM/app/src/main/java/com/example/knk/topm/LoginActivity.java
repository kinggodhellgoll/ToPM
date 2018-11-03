package com.example.knk.topm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void joinClick(View view) {
        startActivity(new Intent(this, JoinActivity.class));
    }

    public void goTest(View view) {
        Intent intent = new Intent(this, MyButtonTestActivity.class);
        startActivity(intent);
    }
}