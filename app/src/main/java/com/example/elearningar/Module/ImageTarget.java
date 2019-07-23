package com.example.elearningar.Module;

public class ImageTarget {

    private String TargetID;
    private String ImagaUrl;
    private String videoUrl;
    private String creatorID;
    private String bookName;
    private String Title;
    private String Description;
    private String likes;


    public ImageTarget() {
    }


    public ImageTarget(String targetID, String imagaUrl, String videoUrl, String creatorID, String bookName, String title, String description, String likes) {
        TargetID = targetID;
        ImagaUrl = imagaUrl;
        this.videoUrl = videoUrl;
        this.creatorID = creatorID;
        this.bookName = bookName;
        Title = title;
        Description = description;
        this.likes = likes;
    }

    public ImageTarget(String creatorID, String bookName, String title, String description) {
        this.creatorID = creatorID;
        this.bookName = bookName;
        Title = title;
        Description = description;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getTargetID() {
        return TargetID;
    }

    public void setTargetID(String targetID) {
        TargetID = targetID;
    }

    public String getImagaUrl() {
        return ImagaUrl;
    }

    public void setImagaUrl(String imagaUrl) {
        ImagaUrl = imagaUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @Override
    public String toString() {
        return "ImageTarget{" +
                "TargetID='" + TargetID + '\'' +
                ", ImagaUrl='" + ImagaUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", bookName='" + bookName + '\'' +
                ", Title='" + Title + '\'' +
                ", Description='" + Description + '\'' +
                ", likes='" + likes + '\'' +
                '}';
    }
}
