package com.example.jobfinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.util.Util;
import com.example.jobfinder.Employee.EmployeeMainActivity;
import com.example.jobfinder.Employee.EmployeeTabbedMainActivity;
import com.example.jobfinder.Employer.EmployerActivity;
import com.example.jobfinder.Employer.EmployerTabbedMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import gr.net.maroulis.library.EasySplashScreen;

public class MainEmptyActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private String userRole;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null){
                    startUserActivityAccordingToRole();
                }
                else{
                    Intent intent = new Intent(MainEmptyActivity.this, ChooseLoginRegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
        Log.i(LOGTAG, "Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }



    private void startUserActivityAccordingToRole(){
        Log.i(LOGTAG, "checkUserRole");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference employerDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer");
        Log.i(LOGTAG, employerDb.toString());
        employerDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.i(LOGTAG, "snapshot");
                        if(snapshot.getKey().equals(user.getUid())){
                            setUserRole("Employer");
                            Log.i(LOGTAG, getUserRole());
                            Intent intent = new Intent(MainEmptyActivity.this, EmployerTabbedMainActivity.class);
                            intent.putExtra("userRole", userRole);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference employeeDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee");
        Log.i(LOGTAG, employeeDb.toString());
        employeeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.i(LOGTAG, "snapshot");
                        if(snapshot.getKey().equals(user.getUid())){
                            setUserRole("Employee");
                            Log.i(LOGTAG, getUserRole());
                            Intent intent = new Intent(MainEmptyActivity.this, EmployeeTabbedMainActivity.class);
                            intent.putExtra("userRole", userRole);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserRole(String userRole){
        this.userRole = userRole;
    }

    private String getUserRole(){
        return userRole;
    }
}