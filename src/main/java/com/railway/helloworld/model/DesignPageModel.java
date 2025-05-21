package com.railway.helloworld.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class DesignPageModel {

    private String phone;

    @JsonProperty("company_name")
    private String companyName;
    private String email;
    private JsonNode bookmark;
    private JsonNode collection;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JsonNode getBookmark() {
        return bookmark;
    }

    public void setBookmark(JsonNode bookmark) {
        this.bookmark = bookmark;
    }

    public JsonNode getCollection() {
        return collection;
    }

    public void setCollection(JsonNode collection) {
        this.collection = collection;
    }
}
