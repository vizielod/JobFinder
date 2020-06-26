package com.example.jobfinder.Employee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employee.EmployeeFragments.PreviewEmployeeProfileFragment;
import com.example.jobfinder.Employer.EmployerFragments.PreviewEmployerProfileFragment;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PreviewEmployeeProfileActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";

    private static FirebaseAuth mAuth;
    private static DatabaseReference mUserDatabase, usersDb;

    private static String userId, employeeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_employee_profile);

        employeeId = getIntent().getExtras().getString("employeeId");

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(employeeId);
        //mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(userId);

        Fragment profileFragment = PreviewEmployeeProfileFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, profileFragment).addToBackStack(null).commit();

    }

    public static String getCurrentUId(){ return userId;}
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }
    public static DatabaseReference getUsersDb() {return usersDb; }
    public static DatabaseReference getUserDatabase() {return mUserDatabase; }

}
