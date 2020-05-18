package com.example.jobfinder.Employer;

public class EmployerObject {
    private String employerId;
    private String employerName;
    private String employerDescription;
    private String employerProfileImageUrl;
    private static String userRole = "Employer";

    public EmployerObject(String employerId, String employerName, String employerDescription, String employerProfileImageUrl){
        this.employerId = employerId;
        this.employerName = employerName;
        this.employerDescription = employerDescription;
        this.employerProfileImageUrl = employerProfileImageUrl;
    }

    public String getEmployerId(){
        return employerId;
    }
    public void setEmployerId(String employerId){
        this.employerId = employerId;
    }

    public String getEmployerName(){
        return employerName;
    }
    public void setEmployerName(String employerName){
        this.employerName = employerName;
    }

    public String getEmployerDescription(){
        return employerDescription;
    }
    public void setEmployerDescription(String employerDescription){
        this.employerDescription = employerDescription;
    }

    public String getEmployerProfileImageUrl(){
        return employerProfileImageUrl;
    }
    public void setEmployerProfileImageUrl(String employerProfileImageUrl){
        this.employerProfileImageUrl = employerProfileImageUrl;
    }

    public static String getUserRole(){ return userRole; }
}
