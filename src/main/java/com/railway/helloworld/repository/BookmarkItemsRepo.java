package com.railway.helloworld.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.TilesModel;

@Repository
public class BookmarkItemsRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public BookmarkItemsRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to BookmarkItemsRepo objects
    private final RowMapper<TilesModel> tilesModelRowMapper = (rs, rowNum) -> {
        TilesModel tilesModel = new TilesModel();
        tilesModel.setProductId(rs.getInt("product_id"));
        tilesModel.setCollection(rs.getString("collection"));
        tilesModel.setName(rs.getString("name"));
        tilesModel.setTexture(rs.getString("texture"));
        tilesModel.setMaterial(rs.getString("material"));
        tilesModel.setSize(rs.getString("size"));
        tilesModel.setSizeAdvance(rs.getString("size_advance"));
        tilesModel.setUnitOfMeasurement(rs.getString("unit_of_measurement"));
        tilesModel.setQuantityPerBox(rs.getInt("quantity_per_box"));
        tilesModel.setCoverage(rs.getFloat("coverage"));
        tilesModel.setUnitPrice(rs.getFloat("unit_price"));
        tilesModel.setWeight(rs.getFloat("weight"));
        tilesModel.setColor(rs.getString("color"));
        tilesModel.setCategories(rs.getString("categories"));
        tilesModel.setImages(rs.getString("images"));
        return tilesModel;
    };

    // Get all products in a specific bookmark
    public ResponseEntity<List<TilesModel>> getAllTilesInBookmark(UUID bookmarkId) {
        String sql = "SELECT t.* FROM tiles t "
                + "JOIN bookmark_items bi ON t.product_id = bi.product_id "
                + "WHERE bi.bookmark_id = ?";
        String checkSql = "SELECT COUNT(*) FROM bookmarks WHERE bookmark_id = ?";
        try {
            // Check if bookmarkId is valid
            if (bookmarkId == null) {
                return ResponseEntity.badRequest().body(null); // Return 400 Bad Request if bookmarkId is null
            }

            // Check if the bookmark exists
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bookmarkId);
            if (count == null || count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if bookmark does not exist
            }

            // Fetch products associated with the bookmark
            List<TilesModel> products = jdbcTemplate.query(sql, tilesModelRowMapper, bookmarkId);
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if no products found
            }
            return ResponseEntity.ok(products); // Return 200 OK with the list of products

        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching products in bookmark_items: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add multiple products to a bookmark in a single query (bulk insert)
    public ResponseEntity<String> addTilesToBookmark(UUID bookmarkId, List<Integer> productIds) {
        // Check if bookmarkId or productIds is null or empty
        if (bookmarkId == null || productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid bookmark_id or product_ids"); // Return 400 Bad Request
        }

        // Check if the bookmark group exists
        String checkBookmarkSql = "SELECT COUNT(*) FROM bookmark_group WHERE bookmark_id = ?";
        Integer bookmarkCount = jdbcTemplate.queryForObject(checkBookmarkSql, Integer.class, bookmarkId);

        if (bookmarkCount == null || bookmarkCount == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark not found"); // Return 404 Not Found
        }

        // Check if products exist in the catalog
        String checkProductSql = "SELECT product_id FROM catalog WHERE product_id IN ("
                + productIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        List<Integer> validProductIds = jdbcTemplate.query(checkProductSql,
                (rs, rowNum) -> rs.getInt("product_id"),
                productIds.toArray()
        );

        if (validProductIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No valid products found"); // Return 404 Not Found
        }

        // Insert multiple products into the bookmark in a single query
        String insertSql = "INSERT INTO bookmark_items (bookmark_id, product_id) VALUES "
                + validProductIds.stream()
                        .map(id -> "(?, ?)")
                        .collect(Collectors.joining(","));

        // Prepare values for batch insertion
        List<Object> params = new ArrayList<>();
        for (Integer productId : validProductIds) {
            params.add(bookmarkId);
            params.add(productId);
        }

        try {
            jdbcTemplate.update(insertSql, params.toArray());
            return ResponseEntity.status(HttpStatus.CREATED).body("Products added to bookmark successfully!"); // Return 201 Created
        } catch (Exception e) {
            System.err.println("Error occurred while adding products to bookmark: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage()); // Return 500 Internal Server Error
        }
    }

    // Remove multiple products from a bookmark in a single query (bulk remove)
    public ResponseEntity<String> removeTilesFromBookmark(UUID bookmarkId, List<Integer> productIds) {
        // Check if bookmarkId or productIds is null or empty
        if (bookmarkId == null || productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid bookmark_id or product_ids"); // Return 400 Bad Request
        }

        // Check if the bookmark exists
        String checkBookmarkSql = "SELECT COUNT(*) FROM bookmarks WHERE bookmark_id = ?";
        Integer bookmarkCount = jdbcTemplate.queryForObject(checkBookmarkSql, Integer.class, bookmarkId);

        if (bookmarkCount == null || bookmarkCount == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark not found"); // Return 404 Not Found
        }

        // Check if the products exist in the tiles table
        String checkProductSql = "SELECT product_id FROM tiles WHERE product_id IN ("
                + productIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        List<Integer> validProductIds = jdbcTemplate.query(checkProductSql,
                (rs, rowNum) -> rs.getInt("product_id"),
                productIds.toArray()
        );

        if (validProductIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No valid products found"); // Return 404 Not Found
        }

        // Delete multiple products from the bookmark in a single query
        String deleteSql = "DELETE FROM bookmark_items WHERE bookmark_id = ? AND product_id IN ("
                + validProductIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        // Prepare parameters for batch deletion
        List<Object> params = new ArrayList<>();
        params.add(bookmarkId);
        params.addAll(validProductIds);

        try {
            int rowsAffected = jdbcTemplate.update(deleteSql, params.toArray());

            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching products found in bookmark"); // Return 404 Not Found
            }

            return ResponseEntity.ok("Products removed from bookmark successfully!"); // Return 200 OK
        } catch (Exception e) {
            System.err.println("Error occurred while removing products from bookmark: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage()); // Return 500 Internal Server Error
        }
    }
}
