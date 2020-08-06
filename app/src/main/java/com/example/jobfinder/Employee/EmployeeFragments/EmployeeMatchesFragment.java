package com.example.jobfinder.Employee.EmployeeFragments;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import com.example.jobfinder.Employee.EmployeeTabbedMainActivity;
import com.example.jobfinder.Employer.JobFragments.JobMatchesFragment;
import com.example.jobfinder.Employer.JobTabbedMainActivity;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesActivity;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesAdapter;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeNewMatchesAdapter;
import com.example.jobfinder.Matches.EmployeeMatches.MatchesJobObject;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobNewMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMatchesFragment extends Fragment {
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private RecyclerView mHorizontalRecyclerView, mRecyclerView;
    private RecyclerView.Adapter mEmployeeMatchesAdapter, mEmployeeNewMatchesAdapter;
    private RecyclerView.LayoutManager mEmployeeMatchesLayoutManager, mEmployeeNewMatchesLayoutManager;

    private String currentUserID, userRole, jobId, employerId;

    private String alertDialogTitle, alertDialogMessage;

    DatabaseReference usersDb, chatDb;

    private Context mContext;
    private FragmentActivity mFragmentActivity;

    private TextView mNewMatchesFoundTV, mContactedMatchesFoundTV, mInterestedTV;

    public EmployeeMatchesFragment(){
        // Required empty public constructor
    }

    public static EmployeeMatchesFragment newInstance(/*String param1, String param2*/) {
        EmployeeMatchesFragment fragment = new EmployeeMatchesFragment();
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
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        mNewMatchesFoundTV = (TextView) view.findViewById(R.id.newMatchesFound_textview);
        mContactedMatchesFoundTV = (TextView) view.findViewById(R.id.contactedMatchesFound_textview);
        mNewMatchesFoundTV.setVisibility(View.VISIBLE);
        mContactedMatchesFoundTV.setVisibility(View.VISIBLE);

        mInterestedTV = (TextView) view.findViewById(R.id.interested_textview);
        mInterestedTV.setText("New Matching Jobs");

        mHorizontalRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_horizontal);
        mHorizontalRecyclerView.setNestedScrollingEnabled(false);
        mHorizontalRecyclerView.setHasFixedSize(true);
        mEmployeeNewMatchesLayoutManager = new LinearLayoutManager(mFragmentActivity, LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(mEmployeeNewMatchesLayoutManager);
        mEmployeeNewMatchesAdapter = new EmployeeNewMatchesAdapter(getDataSetNewMatches(), mFragmentActivity);
        mHorizontalRecyclerView.setAdapter(mEmployeeNewMatchesAdapter);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mEmployeeMatchesLayoutManager = new LinearLayoutManager(mFragmentActivity);
        mRecyclerView.setLayoutManager(mEmployeeMatchesLayoutManager);
        mEmployeeMatchesAdapter = new EmployeeMatchesAdapter(getDataSetContactedMatches(), mFragmentActivity);
        mRecyclerView.setAdapter(mEmployeeMatchesAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);


        //RefreshRecyclerViewList();

        mContext.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mFragmentActivity = getActivity();
        userRole = (String) EmployeeTabbedMainActivity.getUserRole();
        currentUserID = (String) EmployeeTabbedMainActivity.getCurrentUId();
        usersDb = (DatabaseReference) EmployeeTabbedMainActivity.getUsersDb();

        //registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

    @Override
    public void onResume() {
        super.onResume();
        RefreshRecyclerViewList();
    }

    private void RefreshRecyclerViewList(){
        resultsContactedMatches.clear();
        resultsNewMatches.clear();
        mEmployeeMatchesAdapter.notifyDataSetChanged();
        mEmployeeNewMatchesAdapter.notifyDataSetChanged();
        mContactedMatchesFoundTV.setVisibility(View.VISIBLE);
        mNewMatchesFoundTV.setVisibility(View.VISIBLE);
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
        mContext.unregisterReceiver(onDownloadComplete);
        getActivity().finish();
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
                        if(job.child("messaged").getValue().toString().equals("true")){
                            FetchEmployeeMatchInformation(employerId, job.getKey(), false);
                        }
                        else if(job.child("messaged").getValue().toString().equals("false")){
                            FetchEmployeeMatchInformation(employerId, job.getKey(), true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesJobObject> resultsContactedMatches = new ArrayList<MatchesJobObject>();
    private List<MatchesJobObject> getDataSetContactedMatches() {
        return resultsContactedMatches;
    }

    private ArrayList<MatchesJobObject> resultsNewMatches = new ArrayList<MatchesJobObject>();
    private List<MatchesJobObject> getDataSetNewMatches() {
        return resultsNewMatches;
    }

    private int getItemCount(){return resultsContactedMatches.size();}
    private int getNewMatchesItemCount(){return resultsNewMatches.size();}

    /*private ArrayList<MatchesJobObject> resultsEmployeeMatches = new ArrayList<MatchesJobObject>();
    private List<MatchesJobObject> getDataSetEmployeeMatches() {
        return resultsEmployeeMatches;
    }

    private int getItemCount(){return resultsEmployeeMatches.size();}*/

    private void FetchEmployeeMatchInformation(final String employerId, final String jobId, final boolean isNewMatchItem) {
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

                    if(!isNewMatchItem){
                        resultsContactedMatches.add(obj);
                        mEmployeeMatchesAdapter.notifyDataSetChanged();
                        mContactedMatchesFoundTV.setVisibility(View.GONE);
                    }
                    else if(isNewMatchItem){
                        resultsNewMatches.add(obj);
                        mEmployeeNewMatchesAdapter.notifyDataSetChanged();
                        mNewMatchesFoundTV.setVisibility(View.GONE);
                    }
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
        resultsContactedMatches.remove(position);
        mEmployeeMatchesAdapter.notifyItemRemoved(position);
        mEmployeeMatchesAdapter.notifyItemRangeChanged(position, getItemCount());
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage, final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(mFragmentActivity);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    final String employerId = resultsContactedMatches.get(viewHolder.getAdapterPosition()).getEmployerId();
                    final String jobId = resultsContactedMatches.get(viewHolder.getAdapterPosition()).getJobId();
                    deleteMatch(employerId, jobId);
                    removeAt(viewHolder.getAdapterPosition());
                    if(resultsContactedMatches.isEmpty()){
                        mContactedMatchesFoundTV.setVisibility(View.VISIBLE);
                    }
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
