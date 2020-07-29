package com.example.jobfinder.Matches.EmployeeMatches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesViewHolder;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class EmployeeNewMatchesAdapter extends RecyclerView.Adapter<EmployeeMatchesViewHolder>{
    private static final String LOGTAG = "UserRole";
    private List<MatchesJobObject> matchesList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId, jobDescriptionUrl;
    private DatabaseReference usersDb;

    private static File userCVFile;
    private static long downloadID;

    public EmployeeNewMatchesAdapter(List<MatchesJobObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public EmployeeMatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        else{
            Glide.with(context).load(R.drawable.placeholder_img).into(holder.mMatchJobImage);
        }

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
