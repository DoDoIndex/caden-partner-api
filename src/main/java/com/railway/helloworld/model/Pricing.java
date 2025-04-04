package com.railway.helloworld.model;

import java.util.UUID;

public class Pricing {

    private UUID sku;
    private Float unitPrice;

    // Getters and Setters
    public UUID getSku() {
        return sku;
    }

    public void setSku(UUID sku) {
        this.sku = sku;
    }

    public Float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Float unitPrice) {
        this.unitPrice = unitPrice;
    }
}
