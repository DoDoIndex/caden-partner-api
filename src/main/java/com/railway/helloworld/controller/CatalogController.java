package com.railway.helloworld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.repository.PricingRepo;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final PricingRepo pricingRepo;

    // Constructor injection for PricingRepo
    @Autowired
    public CatalogController(PricingRepo pricingRepo) {
        this.pricingRepo = pricingRepo;
    }

    // Check the pricing table in the database
    @GetMapping("/check-pricing-table")
    public String checkPricingTable() {
        return pricingRepo.checkPricingTable();
    }

    @GetMapping("/catalog/sync") // sync and update pricing 
    public String syncCatalog() {
        // Fetch data from export.csv
        // Map partner’s custom pricing to each product_id
        return "Catalog sync completed";
    }

    @GetMapping("/catalog") // product_id, name, unit price, unit of measurement,  image.
    public String getCatalog(@RequestParam(required = false) String category_id) {
        // Fetch catalog data from /api/catalog
        // If category_id is provided, filter by that category
        return "Catalog data for category: " + category_id;
    }

    // @PostMapping("/catalog/price")
    // public String updatePrice(@RequestBody PriceUpdateRequest request) {
    //     // Update partner-specific markup for a given product
    //     return "Price updated";
    // }
    @GetMapping("/product/details")
    public String getProductDetails(@RequestParam String product_id) {
        // Pull from /api/product-info
        // Add partner's price
        return "Product details with pricing";
    }
}
