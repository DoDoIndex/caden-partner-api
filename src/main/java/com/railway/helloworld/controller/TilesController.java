package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.TilesModel;
import com.railway.helloworld.repository.TilesRepo;

@RestController
@RequestMapping("/api/catalog")
public class TilesController {

    private final TilesRepo tilesRepo;

    // Constructor injection for tilesRepo
    @Autowired
    public TilesController(TilesRepo tilesRepo) {
        this.tilesRepo = tilesRepo;
    }

    // Import catalog data from CSV file
    @GetMapping("/sync") // import catalog data from CSV file
    public void importCatalogData() {
        tilesRepo.importDataToCatalog();
    }

    // Get all products in the catalog
    @GetMapping
    public List<TilesModel> getAllProducts() {
        return tilesRepo.getAllProducts().getBody();
    }

    @GetMapping("/product/details")
    public TilesModel getProductDetails(@RequestParam Integer productId) {
        return tilesRepo.getProductDetails(productId).getBody();
    }
}
