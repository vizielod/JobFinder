package com.example.jobfinder;

public class UploadFile {
    public String uploaderID;
    public String fileName;
    public String url;

    public UploadFile(){

    }

    public UploadFile(String uploaderID, String fileName, String url){
        this.uploaderID = uploaderID;
        this.fileName = fileName;
        this.url = url;
    }
    public String getUploaderID() {
        return uploaderID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUploaderID(String uploaderID) {
        this.uploaderID = uploaderID;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
