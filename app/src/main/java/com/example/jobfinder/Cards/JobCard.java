package com.example.jobfinder.Cards;

public class JobCard {
    private String employerId;
    private String jobId;
    private String jobTitle;
    private String jobCategory;
    private String jobImageUrl;

    public JobCard(String employerId, String jobId, String jobTitle, String jobImageUrl){
        this.employerId = employerId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobImageUrl = jobImageUrl;
    }

    public JobCard(String employerId, String jobId, String jobTitle, String jobCategory, String jobImageUrl){
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
    public void setJobID(String userID){
        this.jobId = jobId;
    }

    public String getJobTitle(){
        return jobTitle;
    }
    public void setJobTitle(String jobTitle){
        this.jobTitle = jobTitle;
    }

    public String getJobCategory(){
        return jobCategory;
    }
    public void setJobCategory(String jobCategory){
        this.jobCategory = jobCategory;
    }

    public String getJobImageUrl(){
        return jobImageUrl;
    }
    public void setJobImageUrl(String jobImageUrl){
        this.jobImageUrl = jobImageUrl;
    }

}

