package com.example.jobfinder.Matches.EmployerJobMatches;

public class MatchesEmployeeObject {
    private String userId;
    private String name;
    private String profession;
    private String profileImageUrl;
    private String jobId;
    public MatchesEmployeeObject(String userId, String name, String profession, String profileImageUrl, String jobId){
        this.userId = userId;
        this.name = name;
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
        this.jobId = jobId;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfession(){
        return profession;
    }
    public void setProfession(String profession){
        this.profession = profession;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public String getJobId(){
        return jobId;
    }
    public void setJobID(String jobId){
        this.jobId = jobId;
    }
}
