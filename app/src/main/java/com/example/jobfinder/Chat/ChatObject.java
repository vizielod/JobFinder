package com.example.jobfinder.Chat;

public class ChatObject {
    private String message;
    private Boolean isCurrentUser;
    public ChatObject(String message, Boolean isCurrentUser){
        this.message = message;
        this.isCurrentUser = isCurrentUser;
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String userID){
        this.message = message;
    }

    public Boolean getCurrentUser(){
        return isCurrentUser;
    }
    public void setCurrentUser(Boolean isCurrentUser){
        this.isCurrentUser = isCurrentUser;
    }
}
