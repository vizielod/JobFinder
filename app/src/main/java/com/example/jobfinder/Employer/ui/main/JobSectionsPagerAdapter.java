package com.example.jobfinder.Employer.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.jobfinder.Employer.EmployerFragments.EmployerMainFragment;
import com.example.jobfinder.Employer.EmployerFragments.PreviewEmployerProfileFragment;
import com.example.jobfinder.Employer.JobFragments.JobMainFragment;
import com.example.jobfinder.Employer.JobFragments.JobMatchesFragment;
import com.example.jobfinder.Employer.JobFragments.PreviewJobProfileFragment;
import com.example.jobfinder.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class JobSectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_2, R.string.tab_text_3, R.string.tab_text_4};
    private final Context mContext;

    public JobSectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = PreviewJobProfileFragment.newInstance();
                break;
            case 1:
                fragment = JobMainFragment.newInstance();
                break;
            case 2:
                fragment = JobMatchesFragment.newInstance();
                break;
            case 3:
                fragment = PreviewEmployerProfileFragment.newInstance();
                break;
        }
        return fragment;

        //return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}