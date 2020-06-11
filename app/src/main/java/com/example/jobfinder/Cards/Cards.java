package com.example.jobfinder.Cards;

public class Cards {
    private String userId;
    private String name;
    private String age;
    private String profession;
    private String profileImageUrl;

    public Cards(String userId, String name, String profileImageUrl){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public Cards(String userId, String name, String age, String profession, String profileImageUrl){
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

}

