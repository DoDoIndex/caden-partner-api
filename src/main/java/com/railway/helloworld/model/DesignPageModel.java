package com.railway.helloworld.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class DesignPageModel {

    private String phone;

    @JsonProperty("company_name")
    private String companyName;
    private String email;
    private List<BookmarkItem> bookmark;
    private JsonNode collection;

    public static class BookmarkItem {

        private Integer productId;
        private Double partnerPrice;

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public Double getPartnerPrice() {
            return partnerPrice;
        }

        public void setPartnerPrice(Double partnerPrice) {
            this.partnerPrice = partnerPrice;
        }
    }

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

    public List<BookmarkItem> getBookmark() {
        return bookmark;
    }

    public void setBookmark(List<BookmarkItem> bookmark) {
        this.bookmark = bookmark;
    }

    public JsonNode getCollection() {
        return collection;
    }

    public void setCollection(JsonNode collection) {
        this.collection = collection;
    }
}
