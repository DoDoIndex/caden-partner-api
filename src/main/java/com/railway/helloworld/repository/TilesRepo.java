package com.railway.helloworld.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.railway.helloworld.model.TilesModel;

@Repository
public class TilesRepo {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TilesRepo.class);

    // Constructor injection for JdbcTemplate
    @Autowired
    public TilesRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Error parsing integer: " + e.getMessage());
            return null;
        }
    }

    private Float parseFloat(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0f; // Default value for empty or null strings
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Error parsing float: " + e.getMessage());
            return 0.0f; // Default value in case of parse error
        }
    }

    // Read csv file (deprecated, not used for new structure)
    private List<TilesModel> readTilesFromCSV(String filePath) {
        // Deprecated: not used for new structure
        return new ArrayList<>();
    }

    // Create catalog (insert products from a CSV file into the database)
    public ResponseEntity<String> importDataToCatalog() {
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
                double partnerPrice = unitPrice * 1.3;
                String productDetailsJson = mapper.writeValueAsString(productDetails);
                String sql = "INSERT INTO tiles (product_id, product_details, partner_price) VALUES (?, ?::json, ?) "
                        + "ON CONFLICT (product_id) DO UPDATE SET product_details = EXCLUDED.product_details, partner_price = EXCLUDED.partner_price";
                int rows = jdbcTemplate.update(sql, productId, productDetailsJson, partnerPrice);
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

    // Get all product in catalogs
    public ResponseEntity<List<TilesModel>> getAllProducts() {
        String sql = "SELECT product_id, product_details, partner_price FROM tiles";
        try {
            List<TilesModel> products = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TilesModel tile = new TilesModel();
                tile.setProductId(rs.getInt("product_id"));
                tile.setPartnerPrice(rs.getDouble("partner_price"));
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

    // Get product by ID
    public ResponseEntity<TilesModel> getProductDetails(Integer productId) {
        String sql = "SELECT product_id, product_details, partner_price FROM tiles WHERE product_id = ?";
        try {
            if (productId == null) {
                return ResponseEntity.badRequest().body(null);
            }
            List<TilesModel> tiles = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TilesModel tile = new TilesModel();
                tile.setProductId(rs.getInt("product_id"));
                tile.setPartnerPrice(rs.getDouble("partner_price"));
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

    // Update all partner_price
    public ResponseEntity<String> updatePartnerPrice(Double partnerPrice) {
        String sql = "UPDATE tiles SET partner_price = ?";
        try {
            if (partnerPrice == null) {
                return ResponseEntity.badRequest().body("Invalid partner price");
            }
            int rowsAffected = jdbcTemplate.update(sql, partnerPrice);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found to update");
            }
            return ResponseEntity.ok("All partner_price updated successfully!");
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            logger.error("Error occurred while updating partner_price: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating partner_price");
        }
    }

    // Update partner_price by ProductId
    public ResponseEntity<String> updatePartnerPriceByProductId(Integer productId, Double partnerPrice) {
        String sql = "UPDATE tiles SET partner_price = ? WHERE product_id = ?";
        try {
            if (productId == null || partnerPrice == null) {
                return ResponseEntity.badRequest().body("Invalid productId or partner price");
            }
            int rowsAffected = jdbcTemplate.update(sql, partnerPrice, productId);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found to update");
            }
            return ResponseEntity.ok("Product with ID " + productId + " updated successfully!");
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            logger.error("Error occurred while updating partner_price: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating partner_price");
        }
    }

}
