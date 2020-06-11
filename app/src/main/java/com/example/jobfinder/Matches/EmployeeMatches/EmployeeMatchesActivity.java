package com.example.jobfinder.Matches.EmployeeMatches;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesActivity;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EmployeeMatchesActivity extends AppCompatActivity {
    private static final String DELETE_JOB = "DELETE JOB";

    private static final String LOGTAG = "UserRole";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mEmployeeMatchesAdapter;
    private RecyclerView.LayoutManager mEmployeeMatchesLayoutManager;

    private String currentUserID, userRole;

    private String alertDialogTitle, alertDialogMessage;

    private DatabaseReference usersDb, chatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        userRole = getIntent().getExtras().getString("userRole");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mEmployeeMatchesLayoutManager = new LinearLayoutManager(EmployeeMatchesActivity.this);
        mRecyclerView.setLayoutManager(mEmployeeMatchesLayoutManager);
        mEmployeeMatchesAdapter = new EmployeeMatchesAdapter(getDataSetEmployeeMatches(), EmployeeMatchesActivity.this);
        mRecyclerView.setAdapter(mEmployeeMatchesAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        //getUserMatchId();
        getEmployeeUserMatchId();

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    public BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (EmployeeMatchesAdapter.getDownloadID() == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show();

                /* https://developer.android.com/reference/android/support/v4/content/FileProvider
                https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
                https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
                File tempFile = EmployeeMatchesAdapter.getFile();
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".PDF");
                Uri downloadUri = FileProvider.getUriForFile(EmployeeMatchesActivity.this, EmployeeMatchesActivity.this.getApplicationContext().getPackageName() + ".provider", tempFile);
                Log.i(LOGTAG, downloadUri.toString());
                Intent fileintent = new Intent(Intent.ACTION_QUICK_VIEW);
                fileintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fileintent.setDataAndType(downloadUri, mime);
                startActivity(fileintent);*/
            }
        }
    };

    private void RefreshRecyclerViewList(){
        resultsEmployeeMatches.clear();
        mEmployeeMatchesAdapter.notifyDataSetChanged();
        getEmployeeUserMatchId();
    }


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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    private void getEmployeeUserMatchId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    //Itt a match.getKey() egy employerId
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        //Log.i(LOGTAG, match.get);
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

    //Iterate through every job created by the Employer who's ID = employerID, that has a match with the current employee user
    private void getJobsId(final String employerId) {
        //Log.i(LOGTAG, "getJobsId" + "   " + employerId);
        //DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs");
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("connections").child("matches").child(employerId);
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

    private int getItemCount(){return resultsEmployeeMatches.size();}

    private void FetchEmployeeMatchInformation(final String employerId, final String jobId) {
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(jobId);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String jobId = dataSnapshot.getKey();
                    String jobTitle = "";
                    String jobCategory = "";
                    String jobImageUrl = "";
                    if(dataSnapshot.child("category").getValue()!=null){
                        jobCategory = dataSnapshot.child("category").getValue().toString();
                    }
                    if(dataSnapshot.child("title").getValue()!=null){
                        jobTitle = dataSnapshot.child("title").getValue().toString();
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null){
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }

                    MatchesJobObject obj = new MatchesJobObject(employerId, jobId, jobTitle, jobCategory, jobImageUrl);
                    resultsEmployeeMatches.add(obj);
                    mEmployeeMatchesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void deleteMatch(final String employerId, final String jobId){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference employeeJobMatchDb = usersDb.child(userRole).child(currentUserID).child("connections").child("matches").child(employerId).child(jobId);
        employeeJobMatchDb.addListenerForSingleValueEvent(new ValueEventListener() {
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
        employeeJobMatchDb.removeValue();
        usersDb.child("Employer").child(employerId).child("jobs").child(jobId).child("connections").child("matches").child(currentUserID).removeValue();
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
        resultsEmployeeMatches.remove(position);
        mEmployeeMatchesAdapter.notifyItemRemoved(position);
        mEmployeeMatchesAdapter.notifyItemRangeChanged(position, getItemCount());
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage, final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeMatchesActivity.this);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    final String employerId = resultsEmployeeMatches.get(viewHolder.getAdapterPosition()).getEmployerId();
                    final String jobId = resultsEmployeeMatches.get(viewHolder.getAdapterPosition()).getJobId();
                    deleteMatch(employerId, jobId);
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
