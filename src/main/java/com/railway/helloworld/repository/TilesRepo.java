package com.railway.helloworld.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    // RowMapper to convert database rows to Tiles objects
    private final RowMapper<TilesModel> tilesRowMapper = (rs, rowNum) -> {
        TilesModel tiles = new TilesModel();
        tiles.setProductId(rs.getInt("product_id"));
        tiles.setCollection(rs.getString("collection"));
        tiles.setName(rs.getString("name"));
        tiles.setTexture(rs.getString("texture"));
        tiles.setMaterial(rs.getString("material"));
        tiles.setSize(rs.getString("size"));
        tiles.setSizeAdvance(rs.getString("size_advanced"));
        tiles.setUnitOfMeasurement(rs.getString("unit_of_measurement"));
        tiles.setQuantityPerBox(rs.getInt("quantity_per_box"));
        tiles.setCoverage(rs.getFloat("coverage"));
        tiles.setUnitPrice(rs.getFloat("unit_price"));
        tiles.setMyUnitPrice(rs.getFloat("my_unit_price"));
        tiles.setWeight(rs.getFloat("weight"));
        tiles.setColor(rs.getString("color"));
        tiles.setCategories(rs.getString("categories"));
        tiles.setImages(rs.getString("images"));
        return tiles;
    };

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

    // Read csv file
    private List<TilesModel> readTilesFromCSV(String filePath) {
        List<TilesModel> tilesList = new ArrayList<>();
        String line;
        String delimiter = ","; // Assuming the CSV uses commas as delimiters

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header row if the CSV has one
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] columns = line.split(delimiter);

                // Map the columns to a Tiles object
                TilesModel tiles = new TilesModel();
                tiles.setCollection(columns[1]);
                tiles.setName(columns[11]);
                tiles.setTexture(columns[12]);
                tiles.setMaterial(columns[8]);
                tiles.setSize(columns[9]);
                tiles.setSizeAdvance(columns[10]);
                tiles.setUnitOfMeasurement(columns[3]);
                tiles.setQuantityPerBox(parseInteger(columns[4]));
                tiles.setCoverage(parseFloat(columns[5]));
                tiles.setUnitPrice(parseFloat(columns[6]));
                tiles.setMyUnitPrice(parseFloat(columns[6]) * 1.3f); // Assuming my_unit_price is 30% more than unit_price
                tiles.setWeight(parseFloat(columns[13]));
                tiles.setColor(columns[14]);
                tiles.setCategories(columns[16]);
                tiles.setImages(columns[18]);

                // Add the tiles object to the list
                tilesList.add(tiles);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return tilesList;
    }

    // Create catalog (insert products from a CSV file into the database)
    public ResponseEntity<String> importDataToCatalog() {
        try {
            // Read data from the CSV file
            List<TilesModel> tilesList = readTilesFromCSV("E:\\DaiHoc\\HK6_2024-2025\\T2. IE303 - Cong nghe Java\\Project\\export.csv");

            // Validate the list
            if (tilesList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No data found in the CSV file.");
            }

            // SQL to insert product data into the tiles table
            StringBuilder sqlTiles = new StringBuilder(
                    "INSERT INTO tiles (collection, name, texture, material, size, size_advanced, unit_of_measurement, quantity_per_box, coverage, unit_price, my_unit_price, weight, color, categories, images) VALUES "
            );
            List<Object> params = new ArrayList<>();

            // Loop through tiles and perform inserts
            for (int i = 0; i < tilesList.size(); i++) {
                sqlTiles.append("(");
                sqlTiles.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
                sqlTiles.append(")");

                if (i < tilesList.size() - 1) {
                    sqlTiles.append(", ");
                }

                TilesModel tile = tilesList.get(i);
                params.add(tile.getCollection());
                params.add(tile.getName());
                params.add(tile.getTexture());
                params.add(tile.getMaterial());
                params.add(tile.getSize());
                params.add(tile.getSizeAdvance());
                params.add(tile.getUnitOfMeasurement());
                params.add(tile.getQuantityPerBox());
                params.add(tile.getCoverage());
                params.add(tile.getUnitPrice());
                params.add(tile.getMyUnitPrice());
                params.add(tile.getWeight());
                params.add(tile.getColor());
                params.add(tile.getCategories());
                params.add(tile.getImages());
            }

            // Execute the SQL statement to insert data into the tiles table
            jdbcTemplate.update(sqlTiles.toString(), params.toArray());

            return ResponseEntity.status(HttpStatus.CREATED).body("Catalog created successfully!");
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            logger.error("Error occurred while creating catalog: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while creating catalog: " + e.getMessage());
        }
    }

    // Get all product in catalogs
    public ResponseEntity<List<TilesModel>> getAllProducts() {
        String sql = "SELECT * FROM tiles";
        try {
            // Execute the query to fetch all tiles
            List<TilesModel> tiles = jdbcTemplate.query(sql, tilesRowMapper);
            if (tiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found
            }
            return ResponseEntity.ok(tiles); // Return 200 OK with the list of tiles
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            // Log the error (optional)
            logger.error("Error occurred while fetching tiles: " + e.getMessage());

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

    // Get product by ID
    public ResponseEntity<TilesModel> getProductDetails(Integer productId) {
        String sql = "SELECT * FROM tiles WHERE product_id = ?";
        try {
            // Validate the productId
            if (productId == null) {
                return ResponseEntity.badRequest().body(null); // Return 400 Bad Request
            }

            // Execute the query to fetch the tile by id
            List<TilesModel> tiles = jdbcTemplate.query(sql, tilesRowMapper, productId);
            if (tiles.isEmpty()) {
                return ResponseEntity.status(404).body(null); // Return 404 Not Found
            } else {
                return ResponseEntity.ok(tiles.get(0)); // Return 200 OK with the tiles
            }
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            // Log the error (optional)
            logger.error("Error occurred while fetching tile by ID: " + e.getMessage());

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

    // Update all my_unit_price
    public ResponseEntity<String> updateMyUnitPrice(Float unitPrice) {
        String sql = "UPDATE tiles SET my_unit_price = ?";
        try {
            // Validate the unit price
            if (unitPrice == null) {
                return ResponseEntity.badRequest().body("Invalid unit price"); // Return 400 Bad Request if invalid
            }

            // Update all my_unit_price in the tiles table
            int rowsAffected = jdbcTemplate.update(sql, unitPrice);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found to update"); // Return 404 Not Found if no products exist
            }

            return ResponseEntity.ok("All my_unit_price updated successfully!"); // Return 200 OK if successful
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            // Log the error (optional)
            logger.error("Error occurred while updating my_unit_price: " + e.getMessage());

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating my_unit_price");
        }
    }

    // Update my_unit_price by ProductId
    public ResponseEntity<String> updateMyUnitPriceByProductId(Integer productId, Float unitPrice) {
        String sql = "UPDATE pricing SET unit_price = ? WHERE product_id = ?";
        try {
            // Validate the productId and unit price
            if (productId == null || unitPrice == null) {
                return ResponseEntity.badRequest().body("Invalid productId or unit price"); // Return 400 Bad Request if invalid
            }

            // Update my_unit_price by productId in the pricing table
            int rowsAffected = jdbcTemplate.update(sql, unitPrice, productId);
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found to update"); // Return 404 Not Found if no products exist
            }
            return ResponseEntity.ok("Product with ID " + productId + " updated successfully!"); // Return 200 OK if successful
        } catch (IllegalArgumentException | org.springframework.dao.DataAccessException e) {
            // Log the error (optional)
            logger.error("Error occurred while updating pricing data: " + e.getMessage());

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating pricing data");
        }
    }

}
