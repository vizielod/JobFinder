package com.example.jobfinder.Matches.EmployeeMatches;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMatchesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mEmployeeMatchesAdapter;
    private RecyclerView.LayoutManager mEmployeeMatchesLayoutManager;

    private String currentUserID, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRole = getIntent().getExtras().getString("userRole");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mEmployeeMatchesLayoutManager = new LinearLayoutManager(EmployeeMatchesActivity.this);
        mRecyclerView.setLayoutManager(mEmployeeMatchesLayoutManager);
        mEmployeeMatchesAdapter = new EmployeeMatchesAdapter(getDataSetEmployeeMatches(), EmployeeMatchesActivity.this);
        mRecyclerView.setAdapter(mEmployeeMatchesAdapter);

        //getUserMatchId();
        getEmployeeUserMatchId();
    }

    private void getEmployeeUserMatchId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    //Itt a match.getKey() egy employerId
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        getJobsId(match.getKey());
                        //FetchEmployeeMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getJobsId(final String employerId) {
        //Log.i(LOGTAG, "getJobsId" + "   " + employerId);
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //final String tempEmployerID = employerId;
                if (dataSnapshot.exists()){
                    for(DataSnapshot job : dataSnapshot.getChildren()){
                        FetchEmployeeMatchInformation(employerId, job.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesJobObject> resultsEmployeeMatches = new ArrayList<MatchesJobObject>();
    private List<MatchesJobObject> getDataSetEmployeeMatches() {
        return resultsEmployeeMatches;
    }
    private void FetchEmployeeMatchInformation(final String employerId, final String jobId) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(jobId);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String jobId = dataSnapshot.getKey();
                    String jobTitle = "";
                    String jobImageUrl = "";
                    if(dataSnapshot.child("title").getValue()!=null){
                        jobTitle = dataSnapshot.child("title").getValue().toString();
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null){
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }

                    MatchesJobObject obj = new MatchesJobObject(jobId, jobTitle, jobImageUrl);
                    resultsEmployeeMatches.add(obj);
                    mEmployeeMatchesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
