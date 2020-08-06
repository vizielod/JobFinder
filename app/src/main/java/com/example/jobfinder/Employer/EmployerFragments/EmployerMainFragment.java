package com.example.jobfinder.Employer.EmployerFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employer.EditEmployerProfileActivity;
import com.example.jobfinder.Employer.EmployerActivity;
import com.example.jobfinder.Employer.EmployerTabbedMainActivity;
import com.example.jobfinder.Employer.JobManager;
import com.example.jobfinder.Employer.JobObject;
import com.example.jobfinder.Employer.JobsAdapter;
import com.example.jobfinder.R;
import com.example.jobfinder.SettingsActivity;
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

import java.util.ArrayList;
import java.util.List;

public class EmployerMainFragment extends Fragment {
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb, chatDb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private String userRole;
    private String alertDialogTitle, alertDialogMessage;

    private Context mContext;
    private FragmentActivity mFragmentActivity;
    //private OnFragmentInteractionListener mListener;

    private TextView mEmptyListTV;

    private boolean deleteJobAccepted = false;

    public EmployerMainFragment(){
        // Required empty public constructor
    }

    public static EmployerMainFragment newInstance(/*String param1, String param2*/) {
        EmployerMainFragment fragment = new EmployerMainFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_content_employer, container, false);

        mEmptyListTV = (TextView) view.findViewById(R.id.listEmpty_textview);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_Jobs);
        //mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mJobsLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mJobsLayoutManager);
        mJobsAdapter = new JobsAdapter(getDataSetJobs(), getActivity());
        mRecyclerView.setAdapter(mJobsAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        //getJobsId();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
        mContext = getContext();
        mFragmentActivity = getActivity();
        userRole = (String) EmployerTabbedMainActivity.getUserRole();
        currentUId = (String) EmployerTabbedMainActivity.getCurrentUId();
        usersDb = (DatabaseReference) EmployerTabbedMainActivity.getUsersDb();

    }

    private void RefreshRecyclerViewList(){
        Log.i(LOGTAG, "Back from CreateJobActivity");
        mJobs.clear();
        mJobsAdapter.notifyDataSetChanged();
        getJobsId();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOGTAG, "OnResume");
        /*ViewPager viewPager = (ViewPager) EmployerTabbedMainActivity.getViewPager();
        viewPager.setCurrentItem(1);*/
        //RefreshRecyclerViewList();
        if(getItemCount() == 0){
            mEmptyListTV.setVisibility(View.VISIBLE);
        }
        mJobs.clear();
        mJobsAdapter.notifyDataSetChanged();
        getJobsId();
    }

    /*public static void setEmptyListTVtoVisible(){
        mEmptyListTV.setVisibility(View.VISIBLE);
    }*/

    private void getJobsId() {
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUId).child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot job : dataSnapshot.getChildren()){
                        FetchJobInformation(job.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchJobInformation(String key) {
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUId).child("jobs").child(key);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String jobId = dataSnapshot.getKey();
                    String title = "";
                    String category = "";
                    String jobImageUrl = "";
                    if(dataSnapshot.child("title").getValue()!=null){
                        title = dataSnapshot.child("title").getValue().toString();
                    }
                    if(dataSnapshot.child("category").getValue()!=null){
                        category = dataSnapshot.child("category").getValue().toString();
                    }
                    if(dataSnapshot.child("jobImageUrl").getValue()!=null){
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }
                    JobObject obj = new JobObject(currentUId, jobId, title, category, jobImageUrl);
                    mJobs.add(obj);
                    mJobsAdapter.notifyDataSetChanged();
                    mEmptyListTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<JobObject> mJobs = new ArrayList<JobObject>();
    private List<JobObject> getDataSetJobs() {
        return mJobs;
    }
    private int getItemCount(){return mJobs.size();}

    /*private void deleteJob(JobObject job) {
        mJobs.remove(job);
        mJobsAdapter.notifyDataSetChanged();
    }*/


    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            alertDialogTitle = "Confirm Job Delete";
            alertDialogMessage = "Are you sure you want to DELETE this Job?";
            PopAlerDialogMessage(alertDialogTitle, alertDialogMessage, DELETE_JOB, viewHolder);
            Log.i(LOGTAG, "onSwipe " + mJobs.get(viewHolder.getAdapterPosition()).getJobId());
            /*deleteJob(mJobs.get(viewHolder.getAdapterPosition()).getJobId());
            removeAt(viewHolder.getAdapterPosition());*/
        }
    };

    public void removeAt(int position) {
        mJobs.remove(position);
        Log.i(LOGTAG, String.valueOf(getItemCount()) + "removeat");
        if(getItemCount() == 0){
            mEmptyListTV.setVisibility(View.VISIBLE);
        }
        mJobsAdapter.notifyItemRemoved(position);
        mJobsAdapter.notifyItemRangeChanged(position, getItemCount());
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage, final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //deleteJob(mJobs.get(viewHolder.getAdapterPosition()).getJobId());
                    JobManager.deleteJob(mJobs.get(viewHolder.getAdapterPosition()).getJobId());
                    removeAt(viewHolder.getAdapterPosition());
                    mJobsAdapter.notifyDataSetChanged();
                    Toast.makeText(mContext, "Job successfully deleted", Toast.LENGTH_LONG).show();
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

    /*public void deleteJob(final String jobId){
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
                                Toast.makeText(mContext, "Image deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Image Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mContext, "Image delete failed", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(mContext, "File deleted successfully", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "File Deleted Successfully!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mContext, "File delete failed", Toast.LENGTH_LONG).show();
                                Log.i(LOGTAG, "Error while deleting file!");
                            }
                        });
                    }
                    //delete connections
                    jobDb.removeValue();
                    mJobsAdapter.notifyDataSetChanged();
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
    }*/


}
