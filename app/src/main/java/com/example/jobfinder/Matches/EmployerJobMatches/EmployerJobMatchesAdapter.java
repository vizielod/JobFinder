package com.example.jobfinder.Matches.EmployerJobMatches;

import android.Manifest;
import android.app.Activity;
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.Employee.PreviewEmployeeProfileActivity;
import com.example.jobfinder.Employer.CreateJobActivity;
import com.example.jobfinder.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStorageDirectory;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployerJobMatchesAdapter extends RecyclerView.Adapter<EmployerJobMatchesViewHolder>{
    private static final String LOGTAG = "UserRole";
    private List<MatchesEmployeeObject> matchesList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId, jobDescriptionUrl;
    private DatabaseReference usersDb, chatDb;

    private static File userCVFile;
    private static long downloadID;

    private String alertDialogTitle, alertDialogMessage;

    public EmployerJobMatchesAdapter(List<MatchesEmployeeObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployerJobMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        EmployerJobMatchesViewHolder rcv = new EmployerJobMatchesViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(final EmployerJobMatchesViewHolder holder, final int position) {
        final int temp_position = position;
        holder.mMatchId.setText(matchesList.get(position).getUserId());
        holder.mMatchEmployerId.setText(matchesList.get(position).getEmployerId());
        holder.mMatchName.setText(matchesList.get(position).getName());
        holder.mMatchProfession.setText(matchesList.get(position).getProfession());
        if(!matchesList.get(position).getProfileImageUrl().equals("default")){
            Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
        else{
            Glide.with(context).load(R.drawable.placeholder_img).into(holder.mMatchImage);
        }
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());

        holder.mMatchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PreviewEmployeeProfileActivity.class);
                intent.putExtra("employeeId", matchesList.get(temp_position).getUserId());
                context.startActivity(intent);
                return;
            }
        });

        holder.mGetFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStoragePermission();

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                    return;
                }*/

                //Log.i(LOGTAG, "DELETE" + mJobId.getText().toString());
                usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
                mAuth = FirebaseAuth.getInstance();
                currentUId = mAuth.getCurrentUser().getUid();
                DatabaseReference userDb = usersDb.child("Employee").child(matchesList.get(temp_position).getUserId());
                userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if(dataSnapshot.child("userCVUrl").getValue() != null){
                                final String userCVUrl = dataSnapshot.child("userCVUrl").getValue().toString();
                                if (userCVUrl != null){
                                    //downloadFile(context, matchesList.get(temp_position).getName(), ".pdf", DIRECTORY_DOWNLOADS, userCVUrl);

                                    downloadFile(context, matchesList.get(temp_position).getName(), ".pdf", Environment.getExternalStorageDirectory().getPath(), userCVUrl);
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
                        deleteMatch(holder.mMatchId.getText().toString(), holder.mMatchJobId.getText().toString());
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

    public void removeAt(int position) {
        matchesList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void deleteMatch(final String userID, final String jobId){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference jobEmployeeMatchDb = usersDb.child("Employer").child(currentUId).child("jobs").child(jobId).child("connections").child("matches").child(userID);
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
        usersDb.child("Employee").child(userID).child("connections").child("matches").child(currentUId).child(jobId).removeValue();
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

    public void getStoragePermission(){
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            return;
        }
    }

    // /data/user/0/com.example.jobfinder/cache/Vizi1433498305pdf
    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url){

        userCVFile = new File(destinationDirectory, fileName + "CV" + fileExtension);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + "CV" +fileExtension);
        request.setDestinationUri(Uri.fromFile(userCVFile));

        downloadID = downloadManager.enqueue(request);
        Toast.makeText(context, "Download in progress...", Toast.LENGTH_LONG).show();
        Log.i(LOGTAG, Long.toString(downloadID));
    }

    public void setFile(File file){
        userCVFile = file;
    }

    public static long getDownloadID(){
        return downloadID;
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
