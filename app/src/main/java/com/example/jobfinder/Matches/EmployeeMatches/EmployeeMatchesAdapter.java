package com.example.jobfinder.Matches.EmployeeMatches;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.Employer.CreateJobActivity;
import com.example.jobfinder.Employer.PreviewJobProfileActivity;
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

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployeeMatchesAdapter extends RecyclerView.Adapter<EmployeeMatchesViewHolder> {
    private static final String LOGTAG = "UserRole";
    private List<MatchesJobObject> matchesList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb, chatDb;

    private static File file;
    private static long downloadID;
    private static Uri downloadFileUri;

    private String alertDialogTitle, alertDialogMessage;

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
    public void onBindViewHolder(final EmployeeMatchesViewHolder holder, final int position) {
        final int temp_position = position;
        holder.mMatchEmployerId.setText(matchesList.get(position).getEmployerId());
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());
        holder.mMatchJobTitle.setText(matchesList.get(position).getJobTitle());
        holder.mMatchJobCategory.setText(matchesList.get(position).getJobCategory());
        if (!matchesList.get(position).getJobImageUrl().equals("default")) {
            Glide.with(context).load(matchesList.get(position).getJobImageUrl()).into(holder.mMatchJobImage);
        }
        else{
            Glide.with(context).load(R.drawable.placeholder_img).into(holder.mMatchJobImage);
        }
        //mGetFileButton.setText("Info");
        holder.mMatchJobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PreviewJobProfileActivity.class);
                Bundle b = new Bundle();
                b.putString("jobId", matchesList.get(temp_position).getJobId());
                b.putString("employerId", matchesList.get(temp_position).getEmployerId());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        holder.mGetFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enable Permission for External Storage if it isn't enabled by default.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                    return;
                }
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
                                    downloadFile(context, matchesList.get(temp_position).getJobTitle(), ".pdf", Environment.getExternalStorageDirectory().getPath(), jobDescriptionUrl);
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
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i(LOGTAG, "DELETE" + mJobId.getText().toString());
                alertDialogTitle = "Confirm Job Delete";
                alertDialogMessage = "Are you sure you want to DELETE this Job?";

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(alertDialogTitle);
                builder.setMessage(alertDialogMessage);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeAt(position);
                        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
                        mAuth = FirebaseAuth.getInstance();
                        currentUId = mAuth.getCurrentUser().getUid();
                        deleteMatch(holder.mMatchEmployerId.getText().toString(), holder.mMatchJobId.getText().toString());
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //RefreshRecyclerViewList();
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
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


    public void removeAt(int position) {
        matchesList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void deleteMatch(final String employerId, final String jobId){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference employeeJobMatchDb = usersDb.child("Employee").child(currentUId).child("connections").child("matches").child(employerId).child(jobId);
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
        usersDb.child("Employer").child(employerId).child("jobs").child(jobId).child("connections").child("matches").child(currentUId).removeValue();
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

}