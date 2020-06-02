package com.example.jobfinder.Matches.EmployeeMatches;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Employer.CreateJobActivity;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesViewHolder;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployeeMatchesAdapter extends RecyclerView.Adapter<EmployeeMatchesViewHolder> {
    private static final String LOGTAG = "UserRole";
    private List<MatchesJobObject> matchesList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;

    private static File file;
    private static long downloadID;
    private static Uri downloadFileUri;

    public EmployeeMatchesAdapter(List<MatchesJobObject> matchesList, Context context) {
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployeeMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        EmployeeMatchesViewHolder rcv = new EmployeeMatchesViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(EmployeeMatchesViewHolder holder, int position) {
        final int temp_position = position;
        holder.mMatchEmployerId.setText(matchesList.get(position).getEmployerId());
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());
        holder.mMatchJobTitle.setText(matchesList.get(position).getJobTitle());
        if (!matchesList.get(position).getJobImageUrl().equals("default")) {
            Glide.with(context).load(matchesList.get(position).getJobImageUrl()).into(holder.mMatchJobImage);
        }


        holder.mGetFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i(LOGTAG, "DELETE" + mJobId.getText().toString());
                usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
                mAuth = FirebaseAuth.getInstance();
                currentUId = mAuth.getCurrentUser().getUid();
                DatabaseReference jobDb = usersDb.child("Employer").child(matchesList.get(temp_position).getEmployerId()).child("jobs").child(matchesList.get(temp_position).getJobId());
                jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if(dataSnapshot.child("jobDescriptionUrl").getValue() != null){
                                final String jobDescriptionUrl = dataSnapshot.child("jobDescriptionUrl").getValue().toString();
                                if (jobDescriptionUrl != null) {
                                    downloadFile(context, matchesList.get(temp_position).getJobTitle(), ".pdf", DIRECTORY_DOWNLOADS, jobDescriptionUrl);
                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        //file = new File(context.getExternalFilesDir(null), fileName + "JobDescription" + fileExtension);
        file = new File(destinationDirectory, fileName + "JobDescription" + fileExtension);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + "JobDescription" + fileExtension);
        request.setDestinationUri(Uri.fromFile(file));

        downloadID = downloadManager.enqueue(request);
        downloadFileUri = Uri.fromFile(file);
        /*Log.i(LOGTAG, Long.toString(downloadID));
        Log.i(LOGTAG, downloadFileUri.toString());*/
    }

    public static File getFile(){
        return file;
    }
    public static long getDownloadID(){
        return downloadID;
    }


    public static Uri getDownloadFileUri(){
        return downloadFileUri;
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }

}