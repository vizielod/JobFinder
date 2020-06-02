package com.example.jobfinder.Matches.EmployeeMatches;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

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
    private static final String LOGTAG = "UserRole";
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

    private void FetchEmployeeMatchInformation(final String employerId, final String jobId) {
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(jobId);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    MatchesJobObject obj = new MatchesJobObject(employerId, jobId, jobTitle, jobImageUrl);
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
