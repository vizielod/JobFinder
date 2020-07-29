package com.example.jobfinder.Matches.EmployerJobMatches;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Employer.EmployerActivity;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesAdapter;
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
    private static final String DELETE_JOB = "DELETE JOB";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mEmployerJobMatchesAdapter;
    private RecyclerView.LayoutManager mEmployerJobLayoutManager;

    private String currentUserID, userRole, jobId, employerId;

    private String alertDialogTitle, alertDialogMessage;

    DatabaseReference usersDb, chatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

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
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        //getUserMatchId();
        getEmployerUserJobMatchId();

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //TODO: Implement DELETE on X click
    }

    private void RefreshRecyclerViewList(){
        resultsMatches.clear();
        mEmployerJobMatchesAdapter.notifyDataSetChanged();
        getEmployerUserJobMatchId();
    }

    public BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (EmployeeMatchesAdapter.getDownloadID() == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show();
            }
        }
    };

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            alertDialogTitle = "Confirm Match Delete";
            alertDialogMessage = "Are you sure you want to DELETE this user from the Matches list?";
            PopAlerDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_JOB, viewHolder);
        }
    };

    public String getJobId(){
        return jobId;
    }

    private void getEmployerUserJobMatchId() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches");
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
                    String profession = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profession").getValue()!=null){
                        profession = dataSnapshot.child("profession").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }

                    MatchesEmployeeObject obj = new MatchesEmployeeObject(userId, name, profession, profileImageUrl, jobId, employerId);
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

    private int getItemCount(){return resultsMatches.size();}

    public void deleteMatch(final String userID){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference jobEmployeeMatchDb = usersDb.child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches").child(userID);
        jobEmployeeMatchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String chatID = dataSnapshot.child("chatId").getValue().toString();
                    deleteChatDataOnJobDelete(chatID);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        jobEmployeeMatchDb.removeValue();
        usersDb.child("Employee").child(userID).child("connections").child("matches").child(employerId).child(jobId).removeValue();
    }

    public void deleteChatDataOnJobDelete(final String chatID){
        chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");
        chatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    chatDb.child(chatID).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeAt(int position) {
        resultsMatches.remove(position);
        mEmployerJobMatchesAdapter.notifyItemRemoved(position);
        mEmployerJobMatchesAdapter.notifyItemRangeChanged(position, getItemCount());
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage, final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployerJobMatchesActivity.this);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteMatch(resultsMatches.get(viewHolder.getAdapterPosition()).getUserId());
                    removeAt(viewHolder.getAdapterPosition());
                    dialog.dismiss();
                }
            });
        }

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                RefreshRecyclerViewList();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


}
