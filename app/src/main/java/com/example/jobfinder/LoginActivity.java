package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jobfinder.Employer.EmployerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGTAG = "UserRole";

    private Button mLogin;
    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private DatabaseReference usersDb;

    private String currentUId;

    //usersDb = FirebaseDatabase.getInstance().getReference().child("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null){
                    startUserActivityAccordingToRole();
                }
            }
        };

        mLogin = (Button) findViewById(R.id.login);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOGTAG, "Login");
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                            Log.i(LOGTAG, "sign up error");
                        }

                    }

                });

            }

        });


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

    private String userRole;
    private String oppositeUserRole;

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
                            Intent intent = new Intent(LoginActivity.this, EmployerActivity.class);
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
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
