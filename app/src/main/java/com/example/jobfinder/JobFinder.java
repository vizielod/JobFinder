package com.example.jobfinder;

import android.app.Application;
import android.content.Context;

public class JobFinder extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        JobFinder.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return JobFinder.context;
    }
}
