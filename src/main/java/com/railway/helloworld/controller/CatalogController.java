package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.Catalog;
import com.railway.helloworld.repository.CatalogRepo;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogRepo catalogRepo;

    // Constructor injection for CatalogRepo
    @Autowired
    public CatalogController(CatalogRepo catalogRepo) {
        this.catalogRepo = catalogRepo;
    }

    // Import catalog data from CSV file
    @PostMapping("/sync") // import catalog data from CSV file
    public void importCatalogData() {
        catalogRepo.importDataToCatalog();
    }

    // Get all products in the catalog
    @GetMapping
    public List<Catalog> getAllProducts() {
        return catalogRepo.getAllProducts().getBody();
    }

    @GetMapping("/product/details")
    public Catalog getProductDetails(@RequestParam String sku) {
        return catalogRepo.getProductDetails(sku).getBody();
    }
}
