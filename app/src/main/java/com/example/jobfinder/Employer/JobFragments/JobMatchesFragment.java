package com.example.jobfinder.Employer.JobFragments;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Employer.JobTabbedMainActivity;
import com.example.jobfinder.Employer.JobsAdapter;
import com.example.jobfinder.Matches.EmployeeMatches.EmployeeMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobNewMatchesAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.MatchesEmployeeObject;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class JobMatchesFragment extends Fragment {
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private RecyclerView mHorizontalRecyclerView, mRecyclerView;
    private RecyclerView.Adapter mEmployerJobMatchesAdapter, mEmployerJobNewMatchesAdapter;
    private RecyclerView.LayoutManager mEmployerJobLayoutManager, mEmployerJobNewMatchesLayoutManager;

    private String currentUserID, userRole, jobId, employerId;

    private String alertDialogTitle, alertDialogMessage;

    DatabaseReference usersDb, chatDb;

    private Context mContext;
    private FragmentActivity mFragmentActivity;

    private TextView mNewMatchesFoundTV, mContactedMatchesFoundTV;

    public JobMatchesFragment(){
        // Required empty public constructor
    }

    public static JobMatchesFragment newInstance(/*String param1, String param2*/) {
        JobMatchesFragment fragment = new JobMatchesFragment();
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

        mHorizontalRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_horizontal);
        mHorizontalRecyclerView.setNestedScrollingEnabled(false);
        mHorizontalRecyclerView.setHasFixedSize(true);
        mEmployerJobNewMatchesLayoutManager = new LinearLayoutManager(mFragmentActivity, LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(mEmployerJobNewMatchesLayoutManager);
        mEmployerJobNewMatchesAdapter = new EmployerJobNewMatchesAdapter(getDataSetNewMatches(), mFragmentActivity);
        mHorizontalRecyclerView.setAdapter(mEmployerJobNewMatchesAdapter);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mEmployerJobLayoutManager = new LinearLayoutManager(mFragmentActivity);
        mRecyclerView.setLayoutManager(mEmployerJobLayoutManager);
        mEmployerJobMatchesAdapter = new EmployerJobMatchesAdapter(getDataSetContactedMatches(), mFragmentActivity);
        mRecyclerView.setAdapter(mEmployerJobMatchesAdapter);
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
        userRole = (String) JobTabbedMainActivity.getUserRole();
        currentUserID = (String) JobTabbedMainActivity.getCurrentUId();
        usersDb = (DatabaseReference) JobTabbedMainActivity.getUsersDb();
        jobId = (String) JobTabbedMainActivity.getJobId();
        employerId = (String) JobTabbedMainActivity.getEmployerId();

        //registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshRecyclerViewList();
    }

    private void RefreshRecyclerViewList(){
        resultsContactedMatches.clear();
        resultsNewMatches.clear();
        mEmployerJobMatchesAdapter.notifyDataSetChanged();
        mEmployerJobNewMatchesAdapter.notifyDataSetChanged();
        getEmployerUserJobMatchId();
    }

    public BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (EmployerJobMatchesAdapter.getDownloadID() == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show();
            }
        }
    };

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

    public String getJobId(){
        return jobId;
    }

    private void getEmployerUserJobMatchId() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        if(match.child("messaged").getValue().toString().equals("true")){
                            FetchEmployerJobMatchInformation(match.getKey(), false);
                        }
                        else if(match.child("messaged").getValue().toString().equals("false")){
                            FetchEmployerJobMatchInformation(match.getKey(), true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void FetchEmployerJobMatchInformation(String key, final boolean isNewMatchItem) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profession = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profession").getValue()!=null){
                        profession = dataSnapshot.child("profession").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }

                    MatchesEmployeeObject obj = new MatchesEmployeeObject(userId, name, profession, profileImageUrl, jobId, employerId);

                    if(!isNewMatchItem){
                        resultsContactedMatches.add(obj);
                        mEmployerJobMatchesAdapter.notifyDataSetChanged();
                        mContactedMatchesFoundTV.setVisibility(View.GONE);
                    }
                    else if(isNewMatchItem){
                        resultsNewMatches.add(obj);
                        mEmployerJobNewMatchesAdapter.notifyDataSetChanged();
                        mNewMatchesFoundTV.setVisibility(View.GONE);
                    }
                    /*resultsContactedMatches.add(obj);
                    mEmployerJobMatchesAdapter.notifyDataSetChanged();
                    mEmployerJobNewMatchesAdapter.notifyDataSetChanged();*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<MatchesEmployeeObject> resultsContactedMatches = new ArrayList<MatchesEmployeeObject>();
    private List<MatchesEmployeeObject> getDataSetContactedMatches() {
        return resultsContactedMatches;
    }

    private ArrayList<MatchesEmployeeObject> resultsNewMatches = new ArrayList<MatchesEmployeeObject>();
    private List<MatchesEmployeeObject> getDataSetNewMatches() {
        return resultsNewMatches;
    }

    private int getItemCount(){return resultsContactedMatches.size();}
    private int getNewMatchesItemCount(){return resultsNewMatches.size();}

    public void deleteMatch(final String userID){
        //Itt implementció kérdése, hogy hogyan oldjuk ezt meg.
        //Ha azt szeretnénk, hogy egy match törlése után az Employee-nak ne dobja fel megint azt az állást, akkor nem töröljük az Employee-connections-liked adatbázisából a jobID-t
        //Ha azt szeretnénk, hogy törlés után a Job-hoz már ne dobja fel azt a felhasználót, akkor benne hagyjuk a liked-nál a userID-t
        //Most én annyit csinálok, hogy a matches listában már nem jelenítem meg, de többet nem dobja fel se az Employee felhasználónak se az adott Job-nak a másikat.
        final DatabaseReference jobEmployeeMatchDb = usersDb.child(userRole).child(currentUserID).child("jobs").child(jobId).child("connections").child("matches").child(userID);
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
        usersDb.child("Employee").child(userID).child("connections").child("matches").child(employerId).child(jobId).removeValue();
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
        mEmployerJobMatchesAdapter.notifyItemRemoved(position);
        mEmployerJobMatchesAdapter.notifyItemRangeChanged(position, getItemCount());
    }

    public void PopAlerDialogMessage(String title, String message, String callMessage, final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(mFragmentActivity);

        builder.setTitle(title);
        builder.setMessage(message);

        if(callMessage.equals(DELETE_JOB)){
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteMatch(resultsContactedMatches.get(viewHolder.getAdapterPosition()).getUserId());
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
