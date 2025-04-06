package com.railway.helloworld.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/catalog/sync") // import catalog data from CSV file
    public ResponseEntity<String> importCatalogData() {
        return tilesRepo.importDataToCatalog();
    }

    // Get all products in the catalog
    @GetMapping("/catalog")
    public ResponseEntity<List<TilesModel>> getAllProducts() {
        return tilesRepo.getAllProducts();
    }

    // Get product details by productId 
    @GetMapping("/catalog/product/details") // ?productId=xxx
    public ResponseEntity<TilesModel> getProductDetails(@RequestParam Integer productId) {
        return tilesRepo.getProductDetails(productId);
    }

    // Update all my_unit_price
    @PostMapping("/pricing/update")
    public ResponseEntity<String> updateAllPricing(@RequestBody Float myUnitPrice) {
        return tilesRepo.updateMyUnitPrice(myUnitPrice);
    }

    // Update my_unit_price by productId
    @PostMapping("/pricing/update/{productId}")
    public ResponseEntity<String> updatePricingByProductId(@PathVariable Integer productId, @RequestBody Map<String, Float> body) {
        Float myUnitPrice = body.get("myUnitPrice");
        return tilesRepo.updateMyUnitPriceByProductId(productId, myUnitPrice);
    }
}
