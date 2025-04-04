package com.railway.helloworld.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.Catalog;

@Repository
public class BookmarkGroupRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public BookmarkGroupRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to BookmarkGroup objects
    private final RowMapper<Catalog> catalogRowMapper = (rs, rowNum) -> {
        Catalog catalog = new Catalog();
        catalog.setSku(rs.getString("sku"));
        catalog.setCollection(rs.getString("collection"));
        catalog.setName(rs.getString("name"));
        catalog.setTexture(rs.getString("texture"));
        catalog.setMaterial(rs.getString("material"));
        catalog.setSize(rs.getString("size"));
        catalog.setSizeAdvance(rs.getString("size_advance"));
        catalog.setUnitOfMeasurement(rs.getString("unit_of_measurement"));
        catalog.setQuantityPerBox(rs.getInt("quantity_per_box"));
        catalog.setCoverage(rs.getFloat("coverage"));
        catalog.setUnitPrice(rs.getFloat("unit_price"));
        catalog.setWeight(rs.getFloat("weight"));
        catalog.setColor(rs.getString("color"));
        catalog.setCategories(rs.getString("categories"));
        catalog.setImages(rs.getString("images"));
        return catalog;
    };

    // Get all products in a specific bookmark group
    public ResponseEntity<List<Catalog>> getAllProductsInBookmarkGroup(UUID bookmarkId) {
        String sql = "SELECT c.* FROM catalog c "
                + "JOIN bookmark_group bg ON c.sku = bg.sku "
                + "WHERE bg.bookmark_id = ?";
        String checkSql = "SELECT COUNT(*) FROM bookmark_group WHERE bookmark_id = ?";
        try {
            // Check if bookmarkId is valid
            if (bookmarkId == null) {
                return ResponseEntity.badRequest().body(null); // Return 400 Bad Request if bookmarkId is null
            }

            // Check if the bookmark group exists
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bookmarkId);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if bookmark group does not exist
            }

            // Fetch products associated with the bookmark group
            List<Catalog> products = jdbcTemplate.query(sql, catalogRowMapper, bookmarkId);
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if no products found
            }

            return ResponseEntity.ok(products); // Return 200 OK with the list of products
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching products in bookmark groups: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add a product to a bookmark group
    public ResponseEntity<String> addProductToBookmarkGroup(UUID bookmarkId, String sku) {
        String sql = "INSERT INTO bookmark_group (bookmark_id, sku) VALUES (?, ?)";
        String checkBookmarkSql = "SELECT COUNT(*) FROM bookmark_group WHERE bookmark_id = ?";
        String checkProductSql = "SELECT COUNT(*) FROM catalog WHERE sku = ?";
        try {
            // Check if bookmarkId or sku is null
            if (bookmarkId == null || sku == null || sku.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid bookmark ID or SKU"); // Return 400 Bad Request
            }

            // Check if the bookmark group exists
            Integer bookmarkCount = jdbcTemplate.queryForObject(checkBookmarkSql, Integer.class, bookmarkId);
            if (bookmarkCount == null || bookmarkCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark group not found"); // Return 404 Not Found
            }

            // Check if the product exists in the catalog
            Integer productCount = jdbcTemplate.queryForObject(checkProductSql, Integer.class, sku);
            if (productCount == null || productCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in catalog"); // Return 404 Not Found
            }

            // Insert the product into the bookmark group
            jdbcTemplate.update(sql, bookmarkId, sku);
            return ResponseEntity.status(HttpStatus.CREATED).body("Product added to bookmark group successfully!"); // Return 201 Created
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while adding product to bookmark group: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while adding product to bookmark group: " + e.getMessage());
        }
    }

    // Remove a product from a bookmark group
    public ResponseEntity<String> removeProductFromBookmarkGroup(UUID bookmarkId, String sku) {
        String sql = "DELETE FROM bookmark_group WHERE bookmark_id = ? AND sku = ?";
        String checkBookmarkSql = "SELECT COUNT(*) FROM bookmark_group WHERE bookmark_id = ?";
        String checkProductSql = "SELECT COUNT(*) FROM catalog WHERE sku = ?";
        try {
            // Check if bookmarkId or sku is null
            if (bookmarkId == null || sku == null || sku.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid bookmark ID or SKU"); // Return 400 Bad Request
            }

            // Check if the bookmark group exists
            Integer bookmarkCount = jdbcTemplate.queryForObject(checkBookmarkSql, Integer.class, bookmarkId);
            if (bookmarkCount == null || bookmarkCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark group not found"); // Return 404 Not Found
            }

            // Check if the product exists in the catalog
            Integer productCount = jdbcTemplate.queryForObject(checkProductSql, Integer.class, sku);
            if (productCount == null || productCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in catalog"); // Return 404 Not Found
            }

            // Delete the product from the bookmark group
            int rowsAffected = jdbcTemplate.update(sql, bookmarkId, sku);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in bookmark group"); // Return 404 Not Found
            }

            return ResponseEntity.ok("Product removed from bookmark group successfully!"); // Return 200 OK
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while removing product from bookmark group: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while removing product from bookmark group: " + e.getMessage());
        }
    }
}
