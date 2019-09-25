package com.dinlas.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Intent loginIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}

