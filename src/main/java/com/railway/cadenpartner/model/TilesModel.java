package com.railway.cadenpartner.model;

import java.util.Map;

public class TilesModel {

    private Integer productId;
    private Map<String, Object> productDetails;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Map<String, Object> getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(Map<String, Object> productDetails) {
        this.productDetails = productDetails;
    }
}
