package com.railway.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.PricingModel;

@Repository
public class PricingRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public PricingRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Pricing objects
    private final RowMapper<PricingModel> pricingRowMapper = (rs, rowNum) -> {
        PricingModel pricing = new PricingModel();
        pricing.setProductId(rs.getInt("product_id"));
        pricing.setNewPrice(rs.getFloat("new_price"));
        return pricing;
    };

    // Get all pricing data
    public ResponseEntity<List<PricingModel>> getAllPricing() {
        String sql = "SELECT * FROM pricing";
        try {
            List<PricingModel> pricingList = jdbcTemplate.query(sql, pricingRowMapper);
            return ResponseEntity.ok(pricingList); // Return 200 OK with the list of pricing data
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching pricing data: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get pricing by productId
    public ResponseEntity<Optional<PricingModel>> getPricingByProductId(Integer productId) {
        String sql = "SELECT * FROM pricing WHERE product_id = ?";
        String checkSql = "SELECT COUNT(*) FROM catalog WHERE product_id = ?";
        try {
            // Validate the productId
            if (productId == null) {
                return ResponseEntity.badRequest().body(Optional.empty()); // Return 400 Bad Request if productId is invalid
            }

            // Check if productId is valid
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, productId);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty()); // Return 404 Not Found if productId does not exist
            }

            // Fetch pricing data by productId
            List<PricingModel> pricingList = jdbcTemplate.query(sql, pricingRowMapper, productId);

            if (pricingList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty()); // Return 404 Not Found if no pricing found
            }

            return ResponseEntity.ok(Optional.of(pricingList.get(0))); // Return 200 OK with the pricing data
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching pricing data by productId: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update pricing by ProductId
    public ResponseEntity<String> updatePricingByProductId(Integer productId, Float unitPrice) {
        String sql = "UPDATE pricing SET unit_price = ? WHERE product_id = ?";
        String checkSql = "SELECT COUNT(*) FROM catalog WHERE product_id = ?";
        try {
            // Validate the productId and unit price
            if (productId == null || unitPrice == null) {
                return ResponseEntity.badRequest().body("Invalid productId or unit price"); // Return 400 Bad Request if invalid
            }

            // Check if productId is valid
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, productId);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("productId not found"); // Return 404 Not Found if productId does not exist
            }

            // Update pricing data by productId
            int rowsAffected = jdbcTemplate.update(sql, unitPrice, productId);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("productId not found"); // Return 404 Not Found if productId does not exist
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
