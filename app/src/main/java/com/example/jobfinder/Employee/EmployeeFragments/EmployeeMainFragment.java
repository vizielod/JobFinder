package com.example.jobfinder.Employee.EmployeeFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Cards.Cards;
import com.example.jobfinder.Cards.JobCard;
import com.example.jobfinder.Cards.MyArrayAdapter;
import com.example.jobfinder.Cards.MyJobCardArrayAdapter;
import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.Employee.EmployeeMainActivity;
import com.example.jobfinder.Employee.EmployeeTabbedMainActivity;
import com.example.jobfinder.Employer.JobFragments.JobMainFragment;
import com.example.jobfinder.Employer.JobTabbedMainActivity;
import com.example.jobfinder.Employer.PreviewJobProfileActivity;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMainFragment extends Fragment {
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private FirebaseAuth mAuth;
    private String currentUId, jobId, employerId;
    private DatabaseReference usersDb, chatDb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private String userRole, oppositeUserRole;
    private String alertDialogTitle, alertDialogMessage;

    private Context mContext;
    private FragmentActivity mFragmentActivity;

    private Cards cards_data[];
    private MyArrayAdapter arrayAdapter;
    private MyJobCardArrayAdapter jobArrayAdapter;
    private int i;

    ListView listView;
    List<Cards> rowItems;
    List<JobCard> jobCardItems;

    public EmployeeMainFragment(){
        // Required empty public constructor
    }

    public static EmployeeMainFragment newInstance(/*String param1, String param2*/) {
        EmployeeMainFragment fragment = new EmployeeMainFragment();
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
        View view = inflater.inflate(R.layout.fragment_employee_tabbed_main, container, false);

        checkUserRole();

        jobCardItems = new ArrayList<JobCard>();

        jobArrayAdapter = new MyJobCardArrayAdapter(mFragmentActivity, R.layout.item, jobCardItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.frame);

        flingContainer.setAdapter(jobArrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                jobCardItems.remove(0);
                jobArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                JobCard jobCard = (JobCard) dataObject;
                String employerId = jobCard.getEmployerId();
                String jobId = jobCard.getJobId();
                usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("disliked").child(currentUId).setValue(true);
                Toast.makeText(mFragmentActivity, "Left!", Toast.LENGTH_SHORT).show();
                /*if(jobCardItems.isEmpty()){
                    ImageView anchorImageView = (ImageView) findViewById(R.id.anchorImageView);
                    anchorImageView.setVisibility(View.GONE);
                }*/
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                JobCard jobCard = (JobCard) dataObject;
                String employerId = jobCard.getEmployerId();
                String jobId = jobCard.getJobId();
                usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("liked").child(currentUId).setValue(true);
                isConnectionMatch(employerId, jobId);
                Toast.makeText(mFragmentActivity, "Right!", Toast.LENGTH_SHORT).show();
                /*if(jobCardItems.isEmpty()){
                    ImageView anchorImageView = (ImageView) findViewById(R.id.anchorImageView);
                    anchorImageView.setVisibility(View.GONE);
                }*/
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;*/
                /*ImageView anchorImageView = (ImageView) findViewById(R.id.anchorImageView);
                anchorImageView.setVisibility(View.GONE);*/
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }


        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(mFragmentActivity, "Click!", Toast.LENGTH_SHORT).show();

                JobCard jobCard = (JobCard) dataObject;
                String employerId = jobCard.getEmployerId();
                String jobId = jobCard.getJobId();

                Intent intent = new Intent(mFragmentActivity, PreviewJobProfileActivity.class);
                Bundle b = new Bundle();
                b.putString("jobId", jobId);
                b.putString("employerId", employerId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

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
        userRole = (String) EmployeeTabbedMainActivity.getUserRole();
        currentUId = (String) EmployeeTabbedMainActivity.getCurrentUId();
        usersDb = (DatabaseReference) EmployeeTabbedMainActivity.getUsersDb();

    }

    private void isConnectionMatch(final String employerId, final String jobId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(userRole).child(currentUId).child("connections").child("liked").child(employerId).child(jobId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(mFragmentActivity, "new Connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                    usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("matches").child(currentUId).child("chatId").setValue(key);
                    usersDb.child(oppositeUserRole).child(employerId).child("jobs").child(jobId).child("connections").child("matches").child(currentUId).child("messaged").setValue("false");
                    //usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                    usersDb.child(userRole).child(currentUId).child("connections").child("matches").child(employerId).child(dataSnapshot.getKey()).child("chatId").setValue(key);
                    usersDb.child(userRole).child(currentUId).child("connections").child("matches").child(employerId).child(dataSnapshot.getKey()).child("messaged").setValue("false");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void checkUserRole(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference employeeDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee");
        employeeDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userRole = "Employee";
                    oppositeUserRole = "Employer";
                    //getOppositeRoleUsers();
                    getJobsForEmployee();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void getJobsForEmployee(){
        Log.i(LOGTAG, "getJobsForEmployee");
        DatabaseReference oppositeRoleDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer");
        oppositeRoleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    getJobsId(dataSnapshot.getKey());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getJobsId(final String employerId) {
        Log.i(LOGTAG, "getJobsId" + "   " + employerId);
        DatabaseReference jobsDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs");
        jobsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //final String tempEmployerID = employerId;
                if (dataSnapshot.exists()){
                    for(DataSnapshot job : dataSnapshot.getChildren()){
                        FetchJobInformation(employerId, job.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchJobInformation(final String employerId, final String key) {
        Log.i(LOGTAG, "FetchJobInformation" + "   " + employerId + "   " + key);
        DatabaseReference jobDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(employerId).child("jobs").child(key);
        jobDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").hasChild(currentUId) && !dataSnapshot.child("connections").child("liked").hasChild(currentUId)) {
                    String jobId = dataSnapshot.getKey();
                    String title = "";
                    String category = "";
                    String jobImageUrl = "default";
                    if(dataSnapshot.child("title").getValue()!=null){
                        title = dataSnapshot.child("title").getValue().toString();
                    }
                    if(dataSnapshot.child("category").getValue()!=null){
                        category = dataSnapshot.child("category").getValue().toString();
                    }
                    if (!dataSnapshot.child("jobImageUrl").getValue().equals("default")) {
                        jobImageUrl = dataSnapshot.child("jobImageUrl").getValue().toString();
                    }
                    JobCard item = new JobCard(employerId, jobId, title, category, jobImageUrl);
                    jobCardItems.add(item);
                    jobArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
