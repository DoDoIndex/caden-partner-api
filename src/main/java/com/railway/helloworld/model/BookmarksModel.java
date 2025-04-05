package com.railway.helloworld.model;

import java.util.Date;
import java.util.UUID;

public class BookmarksModel {

    private UUID bookmarkId;
    private String bookmarkName;
    private Date dateCreated;

    // Getters and Setters
    public UUID getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(UUID bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
