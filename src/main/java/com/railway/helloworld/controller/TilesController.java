package com.railway.helloworld.controller;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.railway.helloworld.model.TilesModel;

@RestController
@RequestMapping("/api")
public class TilesController {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TilesController.class);

    @Autowired
    public TilesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Import catalog data from CSV file
    @PostMapping("/catalog/sync")
    public ResponseEntity<String> importCatalogData() {
        String csvFile = "E:/DaiHoc/HK6_2024-2025/T2. IE303 - Cong nghe Java/Project/data.csv";
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            ObjectMapper mapper = new ObjectMapper();
            String[] headers = reader.readNext();
            if (headers == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CSV file is empty or missing header.");
            }
            String[] row;
            int inserted = 0;
            while ((row = reader.readNext()) != null) {
                Map<String, Object> productDetails = new HashMap<>();
                Integer productId = null;
                Double unitPrice = null;
                for (int i = 0; i < headers.length; i++) {
                    String col = headers[i].trim();
                    String val = row[i] != null ? row[i].trim() : null;
                    if (col.equalsIgnoreCase("product_id")) {
                        productId = Integer.parseInt(val);
                    } else if (col.equalsIgnoreCase("Unit Price")) {
                        unitPrice = Double.parseDouble(val);
                        productDetails.put("unit_price", unitPrice);
                    } else {
                        productDetails.put(col, val);
                    }
                }
                if (productId == null || unitPrice == null) {
                    continue;
                }
                String productDetailsJson = mapper.writeValueAsString(productDetails);
                String sql = "INSERT INTO tiles (product_id, product_details) VALUES (?, ?::json) "
                        + "ON CONFLICT (product_id) DO UPDATE SET product_details = EXCLUDED.product_details";
                int rows = jdbcTemplate.update(sql, productId, productDetailsJson);
                if (rows > 0) {
                    inserted++;
                }
            }
            if (inserted == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No data inserted.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("Catalog created successfully! Rows inserted: " + inserted);
        } catch (Exception e) {
            logger.error("Error occurred while creating catalog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while creating catalog: " + e.getMessage());
        }
    }

    // Get all products in the catalog
    @GetMapping("/catalog")
    public ResponseEntity<List<TilesModel>> getAllProducts() {
        String sql = "SELECT product_id, product_details FROM tiles";
        try {
            List<TilesModel> products = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TilesModel tile = new TilesModel();
                tile.setProductId(rs.getInt("product_id"));
                String detailsJson = rs.getString("product_details");
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> details = mapper.readValue(detailsJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
                    tile.setProductDetails(details);
                } catch (Exception ex) {
                    logger.error("Error parsing product_details JSON for product_id " + rs.getInt("product_id") + ": " + ex.getMessage());
                    tile.setProductDetails(null);
                }
                return tile;
            });
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error occurred while fetching tiles: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/catalog/products/{productId}")
    public ResponseEntity<TilesModel> getProductDetails(@PathVariable Integer productId) {
        String sql = "SELECT product_id, product_details FROM tiles WHERE product_id = ?";
        try {
            if (productId == null) {
                return ResponseEntity.badRequest().body(null);
            }
            List<TilesModel> tiles = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TilesModel tile = new TilesModel();
                tile.setProductId(rs.getInt("product_id"));
                String detailsJson = rs.getString("product_details");
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> details = mapper.readValue(detailsJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
                    tile.setProductDetails(details);
                } catch (Exception ex) {
                    logger.error("Error parsing product_details JSON for product_id " + rs.getInt("product_id") + ": " + ex.getMessage());
                    tile.setProductDetails(null);
                }
                return tile;
            }, productId);
            if (tiles.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            } else {
                return ResponseEntity.ok(tiles.get(0));
            }
        } catch (Exception e) {
            logger.error("Error occurred while fetching tile by ID: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/pricing/update/{productId}")
    public ResponseEntity<String> updatePricingByProductId(@PathVariable Integer productId, @RequestBody Map<String, Object> body) {
        String sql = "UPDATE tiles SET partner_price = ? WHERE product_id = ?";
        try {
            if (productId == null) {
                return ResponseEntity.badRequest().body("Invalid productId");
            }

            Double partnerPrice = null;
            Object value = body.get("partnerPrice");
            if (value instanceof Number) {
                partnerPrice = ((Number) value).doubleValue();
            }

            if (partnerPrice == null) {
                return ResponseEntity.badRequest().body("Invalid partner price");
            }

            int rowsAffected = jdbcTemplate.update(sql, partnerPrice, productId);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            return ResponseEntity.ok("Partner price updated successfully!");
        } catch (Exception e) {
            logger.error("Error occurred while updating partner price: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating partner price");
        }
    }

    @GetMapping("/catalog/images")
    public ResponseEntity<List<String>> getAllImages() {
        String sql = "SELECT product_details FROM tiles";
        try {
            Set<String> allImages = new HashSet<>(); // Using Set to avoid duplicates
            List<Map<String, Object>> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
                String detailsJson = rs.getString("product_details");
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(detailsJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception ex) {
                    logger.error("Error parsing product_details JSON: " + ex.getMessage());
                    return null;
                }
            });

            for (Map<String, Object> details : results) {
                if (details != null && details.containsKey("Photo Hover")) {
                    String photoHover = (String) details.get("Photo Hover");
                    if (photoHover != null && !photoHover.trim().isEmpty()) {
                        allImages.add(photoHover.trim());
                    }
                }
            }

            return ResponseEntity.ok(new ArrayList<>(allImages));
        } catch (Exception e) {
            logger.error("Error occurred while fetching images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
