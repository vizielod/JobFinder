package com.example.jobfinder.Employer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinder.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

//dapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
public class JobsAdapter extends RecyclerView.Adapter<JobViewHolder>{
    private static final String LOGTAG = "UserRole";
    private List<JobObject> jobsList;
    private Context context;

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb, chatDb;

    public JobsAdapter(List<JobObject> jobsList, Context context){
        this.jobsList = jobsList;
        this.context = context;
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        JobViewHolder rcv = new JobViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(final JobViewHolder holder, final int position) {
        holder.mJobId.setText(jobsList.get(position).getJobId());
        holder.mJobTitle.setText(jobsList.get(position).getJobTitle());
        if(!jobsList.get(position).getJobImageUrl().equals("default")){
            Glide.with(context).load(jobsList.get(position).getJobImageUrl()).into(holder.mJobImage);
        }
        else{
            Glide.with(context).load(R.mipmap.ic_launcher).into(holder.mJobImage);
        }
        holder.mEmployerId.setText(jobsList.get(position).getEmployerId());

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i(LOGTAG, "DELETE" + mJobId.getText().toString());
                removeAt(position);
                usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
                mAuth = FirebaseAuth.getInstance();
                currentUId = mAuth.getCurrentUser().getUid();
                deleteJob(holder.mJobId.getText().toString());
                //deleteChatAndJobDataFromChatAndEmployeeDb(holder.mJobId.getText().toString());
            }
        });
    }

    public void removeAt(int position) {
        jobsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void deleteJob(final String jobId){
        final DatabaseReference jobDb = usersDb.child("Employer").child(currentUId).child("jobs").child(jobId);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    //Ezt még tesztelni, hogy jól működik-e!
                    deleteJobIdFromEmployeeConnections(jobId);
                    if(dataSnapshot.child("connections").child("matches").exists()){
                        //Job Data must be removed from EmployeeDB and Chat Db
                        //In case if the job is removed but it has connections with Employeee users and if they had
                        //a conversation, all should be removed from the DB. Otherwise it remains there as a "trash"
                        //Same happens in Tinder, also if somebody Disconnects from their pair of Deletes his/her own profile.
                        //The chat and the profile of the User who disconnected is no longer available from the user's profile who was disconnected
                        deleteChatAndJobDataFromChatAndEmployeeDb(jobId);
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null && !dataSnapshot.child("jobImageUrl").getValue().equals("default")){
                        //delete image from storage
                        StorageReference imagefilepath = FirebaseStorage.getInstance().getReference().child("jobImages").child(jobId);
                        imagefilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Image delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting image!");
                            }
                        });
                    }
                    if(dataSnapshot.child("jobDescriptionUrl").getValue()!=null){
                        //delete file from storage
                        StorageReference pdffilepath = FirebaseStorage.getInstance().getReference().child("jobFiles/jobDetailedDescriptions").child(jobId);
                        pdffilepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "File deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "File Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "File delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting file!");
                            }
                        });
                    }
                    //delete connections
                    jobDb.removeValue();
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteChatAndJobDataFromChatAndEmployeeDb(final String jobId) {
        final DatabaseReference jobMatchesConnectionsDb = usersDb.child("Employer").child(currentUId).child("jobs").child(jobId).child("connections").child("matches");
        jobMatchesConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot matchedUser : dataSnapshot.getChildren()){
                        final String employeeID = matchedUser.getKey();
                        //Megkeresni az Employee adatbázisba a getKey által visszaadott ID-val rendelkező felhasználót és annak a connections/matches ágából kitörölni a jobID-t
                        usersDb.child("Employee").child(employeeID).child("connections").child("matches").child(currentUId).child(jobId).removeValue();
                        Log.i(LOGTAG, matchedUser.toString());
                        Log.i(LOGTAG, matchedUser.getKey());
                        if(matchedUser.child("chatId").exists()){
                            final String chatID = matchedUser.child("chatId").getValue().toString();
                            Log.i(LOGTAG, matchedUser.child("chatId").getValue().toString());
                            //A Chat részben megkeresni ezekhez a chatID-khoz tartozó adatot és azokat is eltávolítani
                            deleteChatDataOnJobDelete(chatID);
                        }
                    }
                    //notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void deleteJobIdFromEmployeeConnections(final String jobId){
        final DatabaseReference employeeDb =  usersDb.child("Employee");
        employeeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot employee : dataSnapshot.getChildren()){
                        final String employeeId = employee.getKey();
                        //final DatabaseReference jobsDb = employerDb.child(employerId).child("jobs");
                        if(employee.child("connections").child("liked").exists() && employee.child("connections").child("liked").child(currentUId).child(jobId).exists()){
                            usersDb.child("Employee").child(employeeId).child("connections").child("liked").child(currentUId).child(jobId).removeValue();
                        }
                        else if(employee.child("connections").child("disliked").exists() && employee.child("connections").child("disliked").child(currentUId).child(jobId).exists()){
                            usersDb.child("Employee").child(employeeId).child("connections").child("disliked").child(currentUId).child(jobId).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return this.jobsList.size();
    }


}
