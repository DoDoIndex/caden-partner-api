package com.railway.helloworld.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.PricingModel;
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
    public List<PricingModel> getAllPricing() {
        return pricingRepo.getAllPricing().getBody();
    }

    // Get pricing by productId
    @GetMapping("/{productId}")
    public Optional<PricingModel> getPricingByProdyctId(@PathVariable Integer productId) {
        return pricingRepo.getPricingByProductId(productId).getBody();
    }

    // Update pricing by productId
    @PostMapping("/update/{productId}")
    public void updatePricingByProductId(@PathVariable Integer productId, @RequestBody Float unitPrice) {
        pricingRepo.updatePricingByProductId(productId, unitPrice);
    }
}
