package com.example.jobfinder.Employer;

import android.content.Context;
import android.os.Bundle;

import com.example.jobfinder.Cards.Cards;
import com.example.jobfinder.Cards.MyArrayAdapter;
import com.example.jobfinder.Cards.MyJobCardArrayAdapter;
import com.example.jobfinder.Employer.JobFragments.PreviewJobProfileFragment;
import com.example.jobfinder.Employer.ui.main.JobSectionsPagerAdapter;
import com.example.jobfinder.JobFinder;
import com.example.jobfinder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.jobfinder.Employer.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class JobTabbedMainActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private static FirebaseAuth mAuth;

    private static String currentUId, jobId, employerId;
    private static String userRole = "Employer";

    private static DatabaseReference usersDb, chatDb, mUserDatabase;
    public static ViewPager viewPager;
    public JobSectionsPagerAdapter jobSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_tabbed_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(currentUId);

        jobId = getIntent().getExtras().getString("jobId");
        employerId = getIntent().getExtras().getString("employerId");

        jobSectionsPagerAdapter = new JobSectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(jobSectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    /*@Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        //Fragment fragment = viewPager.getCurrentItem();
        //Fragment fragment = jobSectionsPagerAdapter.getItem(0);
        Log.i(LOGTAG, "onResumeFragments");
        jobSectionsPagerAdapter.notifyDataSetChanged();

    }*/

    public static String getCurrentUId(){ return currentUId;}
    public static String getJobId(){ return jobId;}
    public static String getEmployerId(){ return employerId;}
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }
    public static DatabaseReference getUsersDb() {return usersDb; }
    public static DatabaseReference getChatDb() {return chatDb; }
    public static DatabaseReference getUserDatabase() {return mUserDatabase; }
    public static ViewPager getViewPager() {return viewPager; }
    public static String getUserRole(){ return userRole; }
    public static Context getContext(){ return JobFinder.getAppContext(); }

    public static String getFacebook() { return mUserDatabase.child("facebook").toString();}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}