package com.railway.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.Pricing;

@Repository
public class PricingRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public PricingRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Pricing objects
    private final RowMapper<Pricing> pricingRowMapper = (rs, rowNum) -> {
        Pricing pricing = new Pricing();
        pricing.setSku(rs.getString("sku"));
        pricing.setUnitPrice(rs.getFloat("unit_price"));
        return pricing;
    };

    // Get all pricing data
    public ResponseEntity<List<Pricing>> getAllPricing() {
        String sql = "SELECT * FROM pricing";
        try {
            List<Pricing> pricingList = jdbcTemplate.query(sql, pricingRowMapper);
            return ResponseEntity.ok(pricingList); // Return 200 OK with the list of pricing data
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching pricing data: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get pricing by SKU
    public ResponseEntity<Optional<Pricing>> getPricingBySku(String sku) {
        String sql = "SELECT * FROM pricing WHERE sku = ?";
        String checkSql = "SELECT COUNT(*) FROM catalog WHERE sku = ?";
        try {
            // Validate the SKU
            if (sku == null || sku.isEmpty()) {
                return ResponseEntity.badRequest().body(Optional.empty()); // Return 400 Bad Request if SKU is invalid
            }

            // Check if SKU is valid
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sku);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty()); // Return 404 Not Found if SKU does not exist
            }

            // Fetch pricing data by SKU
            List<Pricing> pricingList = jdbcTemplate.query(sql, pricingRowMapper, sku);

            if (pricingList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty()); // Return 404 Not Found if no pricing found
            }

            return ResponseEntity.ok(Optional.of(pricingList.get(0))); // Return 200 OK with the pricing data
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching pricing data by SKU: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update pricing by SKU
    public ResponseEntity<String> updatePricingBySku(String sku, Float unitPrice) {
        String sql = "UPDATE pricing SET unit_price = ? WHERE sku = ?";
        String checkSql = "SELECT COUNT(*) FROM catalog WHERE sku = ?";
        try {
            // Validate the SKU and unit price
            if (sku == null || sku.isEmpty() || unitPrice == null) {
                return ResponseEntity.badRequest().body("Invalid SKU or unit price"); // Return 400 Bad Request if invalid
            }

            // Check if SKU is valid
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sku);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SKU not found"); // Return 404 Not Found if SKU does not exist
            }

            // Update pricing data by SKU
            int rowsAffected = jdbcTemplate.update(sql, unitPrice, sku);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SKU not found"); // Return 404 Not Found if SKU does not exist
            }

            return ResponseEntity.ok("Pricing updated successfully!"); // Return 200 OK if successful
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while updating pricing data: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating pricing data");
        }
    }
}
