package com.railway.helloworld.model;

import java.util.Date;
import java.util.UUID;

public class Bookmark {

    private UUID bookmarkId;
    private String name;
    private Date createdOn;

    // Getters and Setters
    public UUID getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(UUID bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
