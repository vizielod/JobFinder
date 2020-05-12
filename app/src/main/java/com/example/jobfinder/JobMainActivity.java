package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jobfinder.Cards.Cards;
import com.example.jobfinder.Cards.JobCard;
import com.example.jobfinder.Cards.MyArrayAdapter;
import com.example.jobfinder.Cards.MyJobCardArrayAdapter;
import com.example.jobfinder.Employer.EditJobActivity;
import com.example.jobfinder.Employer.JobObject;
import com.example.jobfinder.Matches.MatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private Cards cards_data[];
    private MyArrayAdapter arrayAdapter;
    private MyJobCardArrayAdapter jobArrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUId;

    private DatabaseReference usersDb;

    ListView listView;
    List<Cards> rowItems;
    List<JobCard> jobCardItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        //checkUserSex();
        checkUserRole();

        rowItems = new ArrayList<Cards>();
        jobCardItems = new ArrayList<JobCard>();

        arrayAdapter = new MyArrayAdapter(this, R.layout.item, rowItems);
        jobArrayAdapter = new MyJobCardArrayAdapter(this, R.layout.item, jobCardItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(jobArrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                //rowItems.remove(0);
                //arrayAdapter.notifyDataSetChanged();
                jobCardItems.remove(0);
                jobArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Cards obj = (Cards) dataObject;
                //String userId = obj.getUserId();
                if(userRole.equals("Employee")){
                    JobCard jobCard = (JobCard) dataObject;
                    String employerId = jobCard.getEmployerId();
                    String jobId = jobCard.getJobId();
                    usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("disliked").child(currentUId).setValue(true);
                }
                else if(userRole.equals("Employer")){
                    Cards obj = (Cards) dataObject;
                    String userId = obj.getUserId();
                    usersDb.child(oppositeUserRole).child(userId).child("connections").child("disliked").child(currentUId).setValue(true);
                }
                Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                //Cards obj = (Cards) dataObject;
                //String userId = obj.getUserId();
                if(userRole.equals("Employee")){
                    JobCard jobCard = (JobCard) dataObject;
                    String employerId = jobCard.getEmployerId();
                    String jobId = jobCard.getJobId();
                    usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("liked").child(currentUId).setValue(true);
                }
                else if(userRole.equals("Employer")){
                    Cards obj = (Cards) dataObject;
                    String userId = obj.getUserId();
                    usersDb.child(oppositeUserRole).child(userId).child("connections").child("liked").child(currentUId).setValue(true);
                }
                //isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;*/
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "Click!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(userRole).child(currentUId).child("connections").child("liked").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "new Connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                    usersDb.child(oppositeUserRole).child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("chatId").setValue(key);
                    //usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                    usersDb.child(userRole).child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("chatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private String userRole;
    private String oppositeUserRole;

    public void checkUserRole(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference employerDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer");

        employerDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userRole = "Employer";
                    oppositeUserRole = "Employee";
                    getOppositeRoleUsers();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        DatabaseReference employeeDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee");

        employeeDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userRole = "Employee";
                    oppositeUserRole = "Employer";
                    //getOppositeRoleUsers();
                    getJobsForEmployee();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void getJobsForEmployee(){
            Log.i(LOGTAG, "getJobsForEmployee");
            DatabaseReference oppositeRoleDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer");
            oppositeRoleDb.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()){
                        /*for (DataSnapshot employer : dataSnapshot.getChildren()){
                            getJobsId(employer.getKey());
                        }*/
                        getJobsId(dataSnapshot.getKey());
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    private void getJobsId(final String employerId) {
        Log.i(LOGTAG, "getJobsId" + "   " + employerId);
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //final String tempEmployerID = employerId;
                if (dataSnapshot.exists()){
                    for(DataSnapshot job : dataSnapshot.getChildren()){
                        FetchJobInformation(employerId, job.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchJobInformation(final String employerId, final String key) {
        Log.i(LOGTAG, "FetchJobInformation" + "   " + employerId + "   " + key);
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(key);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").hasChild(currentUId) && !dataSnapshot.child("connections").child("liked").hasChild(currentUId)) {
                    String jobImageUrl = "default";
                    if (!dataSnapshot.child("jobImageUrl").getValue().equals("default")) {
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }
                    JobCard item = new JobCard(employerId, dataSnapshot.getKey(), dataSnapshot.child("title").getValue().toString(), jobImageUrl);
                    jobCardItems.add(item);
                    jobArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getOppositeRoleUsers(){
        if(userRole.equals("Employee")){
            DatabaseReference oppositeRoleDb = FirebaseDatabase.getInstance().getReference().child("Users").child(oppositeUserRole);
            oppositeRoleDb.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").hasChild(currentUId) && !dataSnapshot.child("connections").child("liked").hasChild(currentUId)) {
                        String profileImageUrl = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Cards item = new Cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        else if(userRole.equals("Employer")){
            DatabaseReference oppositeRoleDb = FirebaseDatabase.getInstance().getReference().child("Users").child(oppositeUserRole);
            oppositeRoleDb.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").hasChild(currentUId) && !dataSnapshot.child("connections").child("liked").hasChild(currentUId)) {
                        String profileImageUrl = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Cards item = new Cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private String userSex;
    private String oppositeUserSex;
    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("sex").getValue() != null){
                        userSex = dataSnapshot.child("sex").getValue().toString();
                        switch (userSex){
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        getOppositeSexUsers();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getOppositeSexUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.child("sex").getValue() != null) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").hasChild(currentUId) && !dataSnapshot.child("connections").child("liked").hasChild(currentUId) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
                        String profileImageUrl = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Cards item = new Cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent();
        if(userRole != null && userRole.equals("Employee")){
            intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("userRole", userRole);
        }
        if(userRole != null && userRole.equals("Employer")){
            intent = new Intent(MainActivity.this, EditJobActivity.class);
            final String jobId = getIntent().getExtras().getString("jobId");
            Log.i(LOGTAG, jobId);
            Bundle b = new Bundle();
            b.putString("userRole", userRole);
            b.putString("jobId", jobId);
            intent.putExtras(b);
        }
        //intent.putExtra("userRole", userRole);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        intent.putExtra("userRole", userRole);
        startActivity(intent);
        return;
    }
}
