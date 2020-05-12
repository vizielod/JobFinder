package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.jobfinder.Employer.EmployerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private Button mRegister;
    private EditText mEmail, mPassword, mName;

    private RadioGroup mRole_RadioGroup, mGender_RadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    //private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    startActivityAfterRegistration();
                    /*Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;*/
                }
            }
        };

        mRegister = (Button) findViewById(R.id.register);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mName = (EditText) findViewById(R.id.name);

        mGender_RadioGroup = (RadioGroup) findViewById(R.id.gender_radioGroup);
        mRole_RadioGroup = (RadioGroup) findViewById(R.id.role_radioGroup);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectRoleID = mRole_RadioGroup.getCheckedRadioButtonId();
                int selectGenderID = mGender_RadioGroup.getCheckedRadioButtonId();

                final RadioButton role_radioButton = (RadioButton) findViewById(selectRoleID);
                final RadioButton gender_radioButton = (RadioButton) findViewById(selectGenderID);

                //innen a gendert majd ki lehet venni kb teljesen, vagy csak ha employer mezőt válassza akkor jelenjen meg az az opció. Másképp itt nem létszükséglet
                if(role_radioButton.getText() == null || gender_radioButton.getText() == null){
                    return;
                }

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(role_radioButton.getText().toString()).child(userId);
                            //userRole = role_radioButton.getText().toString();
                            //Log.i(LOGTAG, userRole);
                            Map userInfo = new HashMap<>();
                            userInfo.put("name", name);
                            userInfo.put("role", role_radioButton.getText().toString());
                            userInfo.put("sex", gender_radioButton.getText().toString());
                            userInfo.put("profileImageUrl", "default");
                            currentUserDb.updateChildren(userInfo);
                        }
                    }
                });
            }
        });
    }

    private void startActivityAfterRegistration(){
        int selectRoleID = mRole_RadioGroup.getCheckedRadioButtonId();
        RadioButton role_radioButton = (RadioButton) findViewById(selectRoleID);
        if(role_radioButton.getText().toString().equals("Employee")){
            Intent intent = new Intent(RegistrationActivity.this, EmployeeMainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        else if(role_radioButton.getText().toString().equals("Employer")){
            final String userRole = role_radioButton.getText().toString();
            Intent intent = new Intent(RegistrationActivity.this, EmployerActivity.class);
            intent.putExtra("userRole", userRole);
            Log.i(LOGTAG, userRole);
            startActivity(intent);
            finish();
            return;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}
