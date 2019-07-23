package com.example.elearningar.Module;

public class KeyWordTarget {

    private String TargetID;
    private String creatorID;
    private String KeyWord;
    private String bookName;
    private String Comment;
    private String FileType;
    private String FileUrl;


    public KeyWordTarget() {
    }

    public KeyWordTarget(String creatorID, String keyWord, String bookName, String comment, String fileType) {
        this.creatorID = creatorID;
        KeyWord = keyWord;
        this.bookName = bookName;
        Comment = comment;
        FileType = fileType;
    }

    public String getTargetID() {
        return TargetID;
    }

    public void setTargetID(String targetID) {
        TargetID = targetID;
    }

    public String getFileUrl() {
        return FileUrl;
    }

    public void setFileUrl(String fileUrl) {
        FileUrl = fileUrl;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getKeyWord() {
        return KeyWord;
    }

    public void setKeyWord(String keyWord) {
        KeyWord = keyWord;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getFileType() {
        return FileType;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    @Override
    public String toString() {
        return "KeyWordTarget{" +
                "TargetID='" + TargetID + '\'' +
                ", FileUrl='" + FileUrl + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", KeyWord='" + KeyWord + '\'' +
                ", bookName='" + bookName + '\'' +
                ", Comment='" + Comment + '\'' +
                ", FileType='" + FileType + '\'' +
                '}';
    }
}
