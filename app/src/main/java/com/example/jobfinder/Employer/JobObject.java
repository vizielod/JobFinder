package com.example.jobfinder.Employer;

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
}
