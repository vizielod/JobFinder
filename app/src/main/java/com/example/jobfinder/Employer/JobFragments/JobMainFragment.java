package com.example.jobfinder.Employer.JobFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.Cards.Cards;
import com.example.jobfinder.Cards.MyArrayAdapter;
import com.example.jobfinder.Cards.MyJobCardArrayAdapter;
import com.example.jobfinder.Chat.ChatActivity;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employee.PreviewEmployeeProfileActivity;
import com.example.jobfinder.Employer.EditJobActivity;
import com.example.jobfinder.Employer.EmployerFragments.EmployerMainFragment;
import com.example.jobfinder.Employer.EmployerTabbedMainActivity;
import com.example.jobfinder.Employer.JobMainActivity;
import com.example.jobfinder.Employer.JobTabbedMainActivity;
import com.example.jobfinder.Employer.JobsAdapter;
import com.example.jobfinder.Matches.EmployerJobMatches.EmployerJobMatchesActivity;
import com.example.jobfinder.R;
import com.example.jobfinder.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class JobMainFragment extends Fragment {
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private FirebaseAuth mAuth;
    private String currentUId, jobId, employerId;
    private DatabaseReference usersDb, chatDb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private TextView mEmployeeListEmptyTV;

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

    public JobMainFragment(){
        // Required empty public constructor
    }

    public static JobMainFragment newInstance(/*String param1, String param2*/) {
        JobMainFragment fragment = new JobMainFragment();
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
        View view = inflater.inflate(R.layout.fragment_job_tabbed_main, container, false);

        getEmployeeUserCards();

        rowItems = new ArrayList<Cards>();

        arrayAdapter = new MyArrayAdapter(mFragmentActivity, R.layout.item, rowItems);
        //arrayAdapter = new MyArrayAdapter(mContext, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.frame);
        mEmployeeListEmptyTV = (TextView) view.findViewById(R.id.listEmpty_textview);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
                /*if(rowItems.isEmpty()){
                    Log.i(LOGTAG, "No Employees Found! Refresh or look back later!");
                }*/
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(oppositeUserRole).child(userId).child("connections").child("disliked").child(employerId).child(jobId).setValue(true);
                Toast.makeText(mFragmentActivity, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(oppositeUserRole).child(userId).child("connections").child("liked").child(employerId).child(jobId).setValue(true);
                isConnectionMatch(userId);
                Toast.makeText(mFragmentActivity, "Right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;*/
                //Log.i(LOGTAG, "No Employees Found! Refresh or look back later!");
                //mEmployeeListEmptyTV.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                Intent intent = new Intent(mFragmentActivity, PreviewEmployeeProfileActivity.class);
                intent.putExtra("employeeId", userId);
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
        //userRole = (String) JobTabbedMainActivity.getUserRole();
        currentUId = (String) JobTabbedMainActivity.getCurrentUId();
        usersDb = (DatabaseReference) JobTabbedMainActivity.getUsersDb();
        jobId = (String) JobTabbedMainActivity.getJobId();
        employerId = (String) JobTabbedMainActivity.getEmployerId();

    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(userRole).child(currentUId).child("jobs").child(jobId).child("connections").child("liked").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(mFragmentActivity, "new Connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                    usersDb.child(oppositeUserRole).child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child(jobId).child("chatId").setValue(key);
                    usersDb.child(oppositeUserRole).child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child(jobId).child("messaged").setValue("false");
                    //usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                    usersDb.child(userRole).child(currentUId).child("jobs").child(jobId).child("connections").child("matches").child(dataSnapshot.getKey()).child("chatId").setValue(key);
                    usersDb.child(userRole).child(currentUId).child("jobs").child(jobId).child("connections").child("matches").child(dataSnapshot.getKey()).child("messaged").setValue("false");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getEmployeeUserCards(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference employerDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer");

        employerDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals(user.getUid())){
                    userRole = "Employer";
                    oppositeUserRole = "Employee";
                    getOppositeRoleUsers();
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

    public void getOppositeRoleUsers(){
        DatabaseReference oppositeRoleDb = FirebaseDatabase.getInstance().getReference().child("Users").child(oppositeUserRole);
        oppositeRoleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("disliked").child(employerId).hasChild(jobId) && !dataSnapshot.child("connections").child("liked").child(employerId).hasChild(jobId)) {
                    String userId = dataSnapshot.getKey();
                    String name = "Name";
                    String age = "Age";
                    String profession = "Profession";
                    String profileImageUrl = "default";

                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("age").getValue()!=null){
                        age = dataSnapshot.child("age").getValue().toString();
                    }
                    if(dataSnapshot.child("profession").getValue()!=null){
                        profession = dataSnapshot.child("profession").getValue().toString();
                    }
                    if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }
                    Cards item = new Cards(userId, name, age, profession, profileImageUrl);
                    rowItems.add(item);
                    arrayAdapter.notifyDataSetChanged();
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
}
