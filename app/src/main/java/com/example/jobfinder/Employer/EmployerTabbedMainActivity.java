package com.example.jobfinder.Employer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.Employer.EmployerFragments.EmployerMainFragment;
import com.example.jobfinder.R;
import com.example.jobfinder.SettingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jobfinder.Employer.ui.main.SectionsPagerAdapter;
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
import java.util.Map;

public class EmployerTabbedMainActivity extends AppCompatActivity
        /*implements EmployerMainFragment.OnFragmentInteractionListener*/{
    private static final String LOGTAG = "UserRole";
    private static final String DELETE_JOB = "DELETE JOB";

    private static FirebaseAuth mAuth;
    private static String currentUId;
    private static DatabaseReference usersDb, chatDb, mUserDatabase;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mJobsAdapter;
    private RecyclerView.LayoutManager mJobsLayoutManager;

    private static String userRole;
    private String alertDialogTitle, alertDialogMessage;

    private EmployerMainFragment mEmployerMainFragment = null;
    public static ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_tabbed_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        userRole = getIntent().getStringExtra("userRole");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employer").child(currentUId);

        //Log.i(LOGTAG, userRole);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(EmployerTabbedMainActivity.this, CreateJobActivity.class);
                startActivity(intent);
                return;
            }

        });
    }

    public static String getUserRole(){
        return userRole;
    }
    public static String getCurrentUId(){ return currentUId;}
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }
    public static DatabaseReference getUsersDb() {return usersDb; }
    public static DatabaseReference getChatDb() {return chatDb; }
    public static DatabaseReference getUserDatabase() {return mUserDatabase; }
    public static ViewPager getViewPager() {return viewPager; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}