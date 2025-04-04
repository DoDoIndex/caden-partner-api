package com.railway.helloworld.model;

import java.util.UUID;

class BookmarkGroup {

    private UUID bookmarkId;
    private String SKU;

    public UUID getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(UUID bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }
}
