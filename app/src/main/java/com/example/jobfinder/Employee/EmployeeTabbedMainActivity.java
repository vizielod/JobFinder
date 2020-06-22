package com.example.jobfinder.Employee;

import android.os.Bundle;

import com.example.jobfinder.Employer.ui.main.JobSectionsPagerAdapter;
import com.example.jobfinder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinder.Employee.ui.main.EmployeeSectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmployeeTabbedMainActivity extends AppCompatActivity {
    private static final String LOGTAG = "UserRole";

    private static FirebaseAuth mAuth;

    private static String currentUId, jobId, employerId;
    private static String userRole = "Employer";

    private static DatabaseReference usersDb, chatDb, mUserDatabase;
    public static ViewPager viewPager;
    public EmployeeSectionsPagerAdapter employeeSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_tabbed_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        userRole = getIntent().getStringExtra("userRole");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(currentUId);

        employeeSectionsPagerAdapter = new EmployeeSectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(employeeSectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
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