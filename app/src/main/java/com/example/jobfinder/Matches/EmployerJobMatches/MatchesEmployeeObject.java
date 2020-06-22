package com.example.jobfinder.Matches.EmployerJobMatches;

public class MatchesEmployeeObject {
    private String userId;
    private String name;
    private String profession;
    private String profileImageUrl;
    private String jobId;



    private String employerId;
    public MatchesEmployeeObject(String userId, String name, String profession, String profileImageUrl, String jobId, String employerId){
        this.userId = userId;
        this.name = name;
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
        this.jobId = jobId;
        this.employerId = employerId;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserId(String userID){
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
    public void setJobId(String jobId){
        this.jobId = jobId;
    }

    public String getEmployerId() {
        return employerId;
    }
    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }


}
