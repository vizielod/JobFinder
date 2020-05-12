package com.example.jobfinder.Matches.EmployeeMatches;

public class MatchesJobObject {
    private String jobId;
    private String jobTitle;
    private String jobImageUrl;
    public MatchesJobObject(String jobId, String jobTitle, String jobImageUrl){
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobImageUrl = jobImageUrl;
    }

    public String getJobId(){
        return jobId;
    }
    public void setJobID(String jobId){
        this.jobId = jobId;
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
