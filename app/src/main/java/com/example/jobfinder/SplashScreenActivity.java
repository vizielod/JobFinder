package com.example.jobfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainEmptyActivity.class)
                .withSplashTimeOut(1000)
                .withHeaderText("JobFinder App")
                .withFooterText("Copyright 2020")
                .withLogo(R.drawable.logo_background_white);

        //change text color
        config.getHeaderTextView().setTextColor(Color.WHITE);
        config.getHeaderTextView().setTextSize(25);
        config.getFooterTextView().setTextColor(Color.WHITE);
        config.getLogo().setMaxHeight(500);
        config.getLogo().setMaxWidth(500);


        //finally create the view
        View easySplashScreenView = config.create();
        setContentView(easySplashScreenView);
    }
}