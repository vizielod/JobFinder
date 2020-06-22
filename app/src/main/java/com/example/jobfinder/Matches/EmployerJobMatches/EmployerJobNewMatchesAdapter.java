package com.example.jobfinder.Matches.EmployerJobMatches;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployerJobNewMatchesAdapter extends RecyclerView.Adapter<EmployerJobMatchesViewHolder>{
    private static final String LOGTAG = "UserRole";
    private List<MatchesEmployeeObject> matchesList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId, jobDescriptionUrl;
    private DatabaseReference usersDb;

    private static File userCVFile;
    private static long downloadID;

    public EmployerJobNewMatchesAdapter(List<MatchesEmployeeObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployerJobMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        EmployerJobMatchesViewHolder rcv = new EmployerJobMatchesViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(EmployerJobMatchesViewHolder holder, int position) {
        final int temp_position = position;
        holder.mMatchId.setText(matchesList.get(position).getUserId());
        holder.mMatchEmployerId.setText(matchesList.get(position).getEmployerId());
        holder.mMatchName.setText(matchesList.get(position).getName());
        //holder.mMatchProfession.setText(matchesList.get(position).getProfession());
        if(!matchesList.get(position).getProfileImageUrl().equals("default")){
            Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
        holder.mMatchJobId.setText(matchesList.get(position).getJobId());

        /*holder.mGetFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });*/
    }
    // /data/user/0/com.example.jobfinder/cache/Vizi1433498305pdf
    /*public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url){

        userCVFile = new File(destinationDirectory, fileName + "CV" + fileExtension);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + "CV" +fileExtension);
        request.setDestinationUri(Uri.fromFile(userCVFile));

        downloadID = downloadManager.enqueue(request);

        Log.i(LOGTAG, Long.toString(downloadID));
    }*/

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
