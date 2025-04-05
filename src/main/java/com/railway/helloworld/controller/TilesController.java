package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.TilesModel;
import com.railway.helloworld.repository.TilesRepo;

@RestController
@RequestMapping("/api")
public class TilesController {

    private final TilesRepo tilesRepo;

    // Constructor injection for tilesRepo
    @Autowired
    public TilesController(TilesRepo tilesRepo) {
        this.tilesRepo = tilesRepo;
    }

    // Import catalog data from CSV file
    @GetMapping("/catalog/sync") // import catalog data from CSV file
    public void importCatalogData() {
        tilesRepo.importDataToCatalog();
    }

    // Get all products in the catalog
    @GetMapping("/catalog")
    public List<TilesModel> getAllProducts() {
        return tilesRepo.getAllProducts().getBody();
    }

    // Get product details by productId
    @GetMapping("/catalog/product/details")
    public TilesModel getProductDetails(@RequestParam Integer productId) {
        return tilesRepo.getProductDetails(productId).getBody();
    }

    // Update all my_unit_price
    @PostMapping("/pricing/update")
    public void updateAllPricing(@RequestBody Float myUnitPrice) {
        tilesRepo.updateMyUnitPrice(myUnitPrice);
    }

    // Update my_unit_price by productId
    @PostMapping("/pricing/update/{productId}")
    public void updatePricingByProductId(@PathVariable Integer productId, @RequestBody Float unitPrice) {
        tilesRepo.updateMyUnitPriceByProductId(productId, unitPrice);
    }
}
