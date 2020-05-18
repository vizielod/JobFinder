package com.example.jobfinder.Employer;

import android.content.Intent;
import android.os.Bundle;

import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployerActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        userRole = getIntent().getStringExtra("userRole");
        Log.i(LOGTAG, userRole);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_Jobs);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mJobsLayoutManager = new LinearLayoutManager(EmployerActivity.this);
        mRecyclerView.setLayoutManager(mJobsLayoutManager);
        mJobsAdapter = new JobsAdapter(getDataSetJobs(), EmployerActivity.this);
        mRecyclerView.setAdapter(mJobsAdapter);
        //getJobsId();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(EmployerActivity.this, CreateJobActivity.class);
                startActivity(intent);
                return;
            }

        });
    }

    private void RefreshRecyclerViewList(){
        Log.i(LOGTAG, "Back from CreateJobActivity");
        resultsJobs.clear();
        mJobsAdapter.notifyDataSetChanged();
        getJobsId();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOGTAG, "OnResume");
        //RefreshRecyclerViewList();
        resultsJobs.clear();
        mJobsAdapter.notifyDataSetChanged();
        getJobsId();
    }

    private void getJobsId() {
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUId).child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot job : dataSnapshot.getChildren()){
                        FetchJobInformation(job.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchJobInformation(String key) {
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUId).child("jobs").child(key);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String jobId = dataSnapshot.getKey();
                    String title = "";
                    String jobImageUrl = "";
                    if(dataSnapshot.child("title").getValue()!=null){
                        title = dataSnapshot.child("title").getValue().toString();
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null){
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }

                    JobObject obj = new JobObject(currentUId, jobId, title, jobImageUrl);
                    resultsJobs.add(obj);
                    mJobsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<JobObject> resultsJobs = new ArrayList<JobObject>();
    private List<JobObject> getDataSetJobs() {
        return resultsJobs;
    }

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(EmployerActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToManageJobs(View view) {
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(EmployerActivity.this, SettingsActivity.class);
        intent.putExtra("userRole", userRole);
        startActivity(intent);
        return;
    }

    public void goToEditProfile(View view) {
        Intent intent = new Intent(EmployerActivity.this, EditEmployerProfileActivity.class);
        startActivity(intent);
        return;
    }
}
