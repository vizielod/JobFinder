package com.example.jobfinder.Employer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.jobfinder.Employee.EmployeeFragments.PreviewEmployeeProfileFragment;
import com.example.jobfinder.Employer.EmployerFragments.PreviewEmployerProfileFragment;
import com.example.jobfinder.R;

public class PreviewEmployerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_employer_profile);

        Fragment profileFragment = PreviewEmployerProfileFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, profileFragment).addToBackStack(null).commit();

        /*FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container,profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();*/
    }
}
