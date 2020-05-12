package com.example.jobfinder.Matches.EmployerJobMatches;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Matches.EmployeeMatches.MatchesJobObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployerJobMatchesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mEmployerJobMatchesAdapter;
    private RecyclerView.LayoutManager mEmployerJobLayoutManager;

    private String cusrrentUserID, userRole, jobId, employerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        cusrrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRole = getIntent().getExtras().getString("userRole");
        jobId = getIntent().getExtras().getString("jobId");
        employerId = getIntent().getExtras().getString("employerId");


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mEmployerJobLayoutManager = new LinearLayoutManager(EmployerJobMatchesActivity.this);
        mRecyclerView.setLayoutManager(mEmployerJobLayoutManager);
        mEmployerJobMatchesAdapter = new EmployerJobMatchesAdapter(getDataSetMatches(), EmployerJobMatchesActivity.this);
        mRecyclerView.setAdapter(mEmployerJobMatchesAdapter);

        //getUserMatchId();
        getEmployerUserJobMatchId();
    }

    private void getEmployerUserJobMatchId() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(cusrrentUserID).child("jobs").child(jobId).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        FetchEmployerJobMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void FetchEmployerJobMatchInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }

                    MatchesEmployeeObject obj = new MatchesEmployeeObject(userId, name, profileImageUrl);
                    resultsMatches.add(obj);
                    mEmployerJobMatchesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<MatchesEmployeeObject> resultsMatches = new ArrayList<MatchesEmployeeObject>();
    private List<MatchesEmployeeObject> getDataSetMatches() {
        return resultsMatches;
    }



}
