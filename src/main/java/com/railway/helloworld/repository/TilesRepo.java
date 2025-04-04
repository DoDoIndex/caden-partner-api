package com.railway.helloworld.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        tiles.setSizeAdvance(rs.getString("size_advance"));
        tiles.setUnitOfMeasurement(rs.getString("unit_of_measurement"));
        tiles.setQuantityPerBox(rs.getInt("quantity_per_box"));
        tiles.setCoverage(rs.getFloat("coverage"));
        tiles.setUnitPrice(rs.getFloat("unit_price"));
        tiles.setWeight(rs.getFloat("weight"));
        tiles.setColor(rs.getString("color"));
        tiles.setCategories(rs.getString("categories"));
        tiles.setImages(rs.getString("images"));
        return tiles;
    };

    private Float parseFloat(String value) {
        try {
            return (value == null || value.isEmpty()) ? 0.0f : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid float value: " + value);
            return 0.0f; // Default value
        }
    }

    private Integer parseInteger(String value) {
        try {
            return (value == null || value.isEmpty()) ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer value: " + value);
            return 0; // Default value
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
                tiles.setQuantityPerBox(parseInteger(columns[4])); // Use helper method
                tiles.setCoverage(parseFloat(columns[5])); // Use helper method
                tiles.setUnitPrice(parseFloat(columns[6])); // Use helper method
                tiles.setWeight(parseFloat(columns[13])); // Use helper method
                tiles.setColor(columns[14]);
                tiles.setCategories(columns[16]);
                tiles.setImages(columns[18]);

                // Add the tiles object to the list
                tilesList.add(tiles);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            String sqlTiles = "INSERT INTO tiles (collection, name, texture, material, size, size_advance, unit_of_measurement, quantity_per_box, coverage, unit_price, weight, color, categories, images) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "collection = EXCLUDED.collection, "
                    + "name = EXCLUDED.name, "
                    + "texture = EXCLUDED.texture, "
                    + "material = EXCLUDED.material, "
                    + "size = EXCLUDED.size, "
                    + "size_advance = EXCLUDED.size_advance, "
                    + "unit_of_measurement = EXCLUDED.unit_of_measurement, "
                    + "quantity_per_box = EXCLUDED.quantity_per_box, "
                    + "coverage = EXCLUDED.coverage, "
                    + "unit_price = EXCLUDED.unit_price, "
                    + "weight = EXCLUDED.weight, "
                    + "color = EXCLUDED.color, "
                    + "categories = EXCLUDED.categories, "
                    + "images = EXCLUDED.images";

            // SQL to insert/update pricing data
            String sqlPricing = "INSERT INTO pricing (product_id, unit_price) VALUES (?, ?) "
                    + "ON CONFLICT (product_id) DO UPDATE SET unit_price = EXCLUDED.unit_price";

            // Loop through tiles and perform inserts
            for (TilesModel tile : tilesList) {
                // Insert or update tile info
                jdbcTemplate.update(sqlTiles,
                        tile.getProductId(),
                        tile.getCollection(),
                        tile.getName(),
                        tile.getTexture(),
                        tile.getMaterial(),
                        tile.getSize(),
                        tile.getSizeAdvance(),
                        tile.getUnitOfMeasurement(),
                        tile.getQuantityPerBox(),
                        tile.getCoverage(),
                        tile.getUnitPrice(),
                        tile.getWeight(),
                        tile.getColor(),
                        tile.getCategories(),
                        tile.getImages()
                );

                // Insert or update pricing with 30% markup
                jdbcTemplate.update(sqlPricing,
                        tile.getProductId(),
                        tile.getUnitPrice() * 1.3f
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Catalog created successfully!");
        } catch (Exception e) {
            System.err.println("Error occurred while creating catalog: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while creating catalog: " + e.getMessage());
        }
    }

    // Get all product in catalogs
    public ResponseEntity<List<TilesModel>> getAllProducts() {
        String sql = "SELECT * FROM tiles";
        try {
            List<TilesModel> tilesModel = jdbcTemplate.query(sql, tilesRowMapper);
            return ResponseEntity.ok(tilesModel); // Return 200 OK with the list of tiles
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching tiles: " + e.getMessage());
            e.printStackTrace();

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
                return ResponseEntity.ok(tiles.get(0)); // Return 200 OK with the tilesModel
            }
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching tile by ID: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

}
