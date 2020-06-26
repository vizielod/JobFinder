package com.example.jobfinder.Employer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Employer.JobFragments.PreviewJobProfileFragment;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PreviewJobProfileActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";
    final static int PICK_PDF_CODE = 2342;
    
    private static FirebaseAuth mAuth;
    private static DatabaseReference mUserDatabase, usersDb;

    private static String userId, jobId, employerId;

    private static FragmentManager fm;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_job_profile);

        jobId = getIntent().getExtras().getString("jobId");
        employerId = getIntent().getExtras().getString("employerId");
        Log.i(LOGTAG, jobId);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId);

        Fragment profileFragment = PreviewJobProfileFragment.newInstance();
        fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, profileFragment).addToBackStack(null).commit();


    }

    public static FragmentManager getMyFragmentManager(){return fm;}
    public static String getJobId(){return jobId;}
    public static String getEmployerId(){return employerId;}
    public static String getCurrentUId(){ return userId;}
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }
    public static DatabaseReference getUsersDb() {return usersDb; }
    public static DatabaseReference getUserDatabase() {return mUserDatabase; }

}
