package com.railway.cadenpartner.model;

import lombok.Data;

@Data
public class ChatRequest {

    private String searchType;  // "Material", "Color Group", "Size", "Usage", or "Trim"
    private String searchValue; // The value to search for

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }
}
