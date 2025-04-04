package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.Pricing;
import com.railway.helloworld.repository.PricingRepo;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingRepo pricingRepo;

    // Constructor injection for PricingRepo
    @Autowired
    public PricingController(PricingRepo pricingRepo) {
        this.pricingRepo = pricingRepo;
    }

    // Get all pricing data
    @GetMapping
    public List<Pricing> getAllPricing() {
        return pricingRepo.getAllPricing().getBody();
    }

    // Get pricing by SKU
    @GetMapping("/{sku}")
    public Pricing getPricingBySku(@PathVariable String sku) {
        return pricingRepo.getPricingBySku(sku).getBody().orElse(null);
    }

    // Update pricing by SKU
    @PostMapping("/update/{sku}")
    public void updatePricingBySku(@PathVariable String sku, @RequestBody Float unitPrice) {
        pricingRepo.updatePricingBySku(sku, unitPrice);
    }
}
