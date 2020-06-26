package com.example.jobfinder.Employer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import com.example.jobfinder.Employee.EmployeeFragments.PreviewEmployeeProfileFragment;
import com.example.jobfinder.Employer.EmployerFragments.PreviewEmployerProfileFragment;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PreviewEmployerProfileActivity extends AppCompatActivity {

    private static FirebaseAuth mAuth;
    private static DatabaseReference mUserDatabase, usersDb;

    private static String userId, employerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_employer_profile);

        employerId = getIntent().getExtras().getString("employerId");

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId);
        //mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);

        Fragment profileFragment = PreviewEmployerProfileFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, profileFragment).addToBackStack(null).commit();

    }

    public static String getCurrentUId(){ return userId;}
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }
    public static DatabaseReference getUsersDb() {return usersDb; }
    public static DatabaseReference getUserDatabase() {return mUserDatabase; }
}
