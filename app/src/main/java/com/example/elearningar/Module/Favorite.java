package com.example.elearningar.Module;

public class Favorite {

    private String TargetID;
    private String FavoriteType;
    private ImageTarget imageTarget;
    private KeyWordTarget keyWordTarget;
    private Comment comment;
    boolean offline;
    private String dateAdded;


    public Favorite() {
    }

    public Favorite(String targetID, String favoriteType, ImageTarget imageTarget, KeyWordTarget keyWordTarget, Comment comment, boolean ofline) {
        TargetID = targetID;
        FavoriteType = favoriteType;
        this.imageTarget = imageTarget;
        this.keyWordTarget = keyWordTarget;
        this.comment = comment;
        this.offline = ofline;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getTargetID() {
        return TargetID;
    }

    public void setTargetID(String targetID) {
        TargetID = targetID;
    }

    public String getFavoriteType() {
        return FavoriteType;
    }

    public void setFavoriteType(String favoriteType) {
        FavoriteType = favoriteType;
    }

    public ImageTarget getImageTarget() {
        return imageTarget;
    }

    public void setImageTarget(ImageTarget imageTarget) {
        this.imageTarget = imageTarget;
    }

    public KeyWordTarget getKeyWordTarget() {
        return keyWordTarget;
    }

    public void setKeyWordTarget(KeyWordTarget keyWordTarget) {
        this.keyWordTarget = keyWordTarget;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }


    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean ofline) {
        this.offline = ofline;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "TargetID='" + TargetID + '\'' +
                ", FavoriteType='" + FavoriteType + '\'' +
                ", imageTarget=" + imageTarget +
                ", keyWordTarget=" + keyWordTarget +
                ", comment=" + comment +
                ", offline=" + offline +
                ", dateAdded='" + dateAdded + '\'' +
                '}';
    }
}
