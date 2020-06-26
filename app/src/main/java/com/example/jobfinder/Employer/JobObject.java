package com.example.jobfinder.Employer;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class JobObject {
    private String employerId;
    private String jobId;
    private String jobTitle;
    private String jobImageUrl;
    private String jobCategory;

    public JobObject(String employerId, String jobId, String jobTitle, String jobCategory, String jobImageUrl){
        this.employerId = employerId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobCategory = jobCategory;
        this.jobImageUrl = jobImageUrl;
    }

    public String getEmployerId(){
        return employerId;
    }
    public void setEmployerId(String employerId){
        this.employerId = employerId;
    }

    public String getJobId(){
        return jobId;
    }
    public void setJobId(String jobId){
        this.jobId = jobId;
    }

    public String getJobCategory(){
        return jobCategory;
    }
    public void setJobCategory(String jobCategory){
        this.jobCategory = jobCategory;
    }

    public String getJobTitle(){
        return jobTitle;
    }
    public void setJobTitle(String jobTitle){
        this.jobTitle = jobTitle;
    }

    public String getJobImageUrl(){
        return jobImageUrl;
    }
    public void setJobImageUrl(String jobImageUrl){
        this.jobImageUrl = jobImageUrl;
    }

    public static void populateJobCategorySpinnerList(ArrayList<String> jobCategorySpinnerList){
        jobCategorySpinnerList.add("IT & Software");
        jobCategorySpinnerList.add("Sales and client care");
        jobCategorySpinnerList.add("Operations");
        jobCategorySpinnerList.add("Medicine & Health");
        jobCategorySpinnerList.add("Social Care");
        jobCategorySpinnerList.add("Accounting & Finance");
        jobCategorySpinnerList.add("Mechanical engineering");
        jobCategorySpinnerList.add("Marketing, Language & Communication");
        jobCategorySpinnerList.add("Electrical engineering");
        jobCategorySpinnerList.add("Business & Strategy");
        jobCategorySpinnerList.add("Supply Chain & Logistics");
        jobCategorySpinnerList.add("Administration");
        jobCategorySpinnerList.add("Education & training");
        jobCategorySpinnerList.add("Human Resources");
        jobCategorySpinnerList.add("Architecture & Construction");
        jobCategorySpinnerList.add("Climate, Environment & Sustainability");
        jobCategorySpinnerList.add("Legal");
        jobCategorySpinnerList.add("Biology, Biotech & Biochemistry");
        jobCategorySpinnerList.add("Quality assurance & risk");
        jobCategorySpinnerList.add("Creative & design");
        jobCategorySpinnerList.add("Hospitality & Tourism");
        jobCategorySpinnerList.add("Retail");
        jobCategorySpinnerList.add("Agriculture, forestry & marine");
        jobCategorySpinnerList.add("Chemistry & chemical engineering");
        jobCategorySpinnerList.add("Culture & Arts");
        jobCategorySpinnerList.add("Society & Politics");
        jobCategorySpinnerList.add("Consulting");
        jobCategorySpinnerList.add("Mathematics & Physics");
        jobCategorySpinnerList.add("Oil, Gas & Energy");
        jobCategorySpinnerList.add("Real Estate");
        jobCategorySpinnerList.add("Veterinary & Animal Sciences");

        Collections.sort(jobCategorySpinnerList);
        jobCategorySpinnerList.add("Other");
    }

    public static void populateJobTypeSpinnerList(ArrayList<String> jobTypeSpinnerList){
        jobTypeSpinnerList.add("Full-time");
        jobTypeSpinnerList.add("Part-time");
        jobTypeSpinnerList.add("Student worker");
        jobTypeSpinnerList.add("Internship");
        jobTypeSpinnerList.add("Co-founder");
        jobTypeSpinnerList.add("Freelance");
        jobTypeSpinnerList.add("Project");
        jobTypeSpinnerList.add("Voluntary Work");
        jobTypeSpinnerList.add("Thesis");
        jobTypeSpinnerList.add("Phd / Research");
        jobTypeSpinnerList.add("Temporary");
        jobTypeSpinnerList.add("Graduate programme");

        Collections.sort(jobTypeSpinnerList);
        jobTypeSpinnerList.add("Other");
    }
}
