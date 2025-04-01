package com.railway.helloworld.model;

public class BookmarkItemRequest {

    private String productId;  // The ID of the product being added or removed from the bookmark group

    // Constructor
    public BookmarkItemRequest(String productId) {
        this.productId = productId;
    }

    // Getter and Setter for productId
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
// This class represents a request to add or remove an item from a bookmark group. It contains the product ID of the item.
