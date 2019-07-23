package com.example.elearningar.Module;



public class Comment {
    private String commentId;
    private String commentUserId;
    private String commentText;
    private String commentDate;
    private String likes;
    private String TargetID;

    private String AttachmentUrl;
    private String AttachmentType;

    public Comment() {
    }

    public Comment(String commentUserId, String commentText, String commentDate) {
        this.commentId = commentId;
        this.commentUserId = commentUserId;
        this.commentText = commentText;
        this.commentDate = commentDate;
    }

    public Comment(String commentUserId, String commentText, String commentDate ,String attachmentType) {
        this.commentId = commentId;
        this.commentUserId = commentUserId;
        this.commentText = commentText;
        this.commentDate = commentDate;
        AttachmentType = attachmentType;
    }


    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }


    public String getCommentUserId() {
        return commentUserId;
    }

    public void setCommentUserId(String commentUserId) {
        this.commentUserId = commentUserId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
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

    public String getAttachmentUrl() {
        return AttachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        AttachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return AttachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        AttachmentType = attachmentType;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", commentUserId='" + commentUserId + '\'' +
                ", commentText='" + commentText + '\'' +
                ", commentDate='" + commentDate + '\'' +
                ", likes='" + likes + '\'' +
                ", TargetID='" + TargetID + '\'' +
                ", AttachmentUrl='" + AttachmentUrl + '\'' +
                ", AttachmentType='" + AttachmentType + '\'' +
                '}';
    }
}
