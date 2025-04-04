package com.railway.helloworld.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.Catalog;

@Repository
public class CatalogRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public CatalogRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Catalog objects
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
    private List<Catalog> readCatalogFromCSV(String filePath) {
        List<Catalog> catalogList = new ArrayList<>();
        String line;
        String delimiter = ","; // Assuming the CSV uses commas as delimiters

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header row if the CSV has one
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] columns = line.split(delimiter);

                // Map the columns to a Catalog object
                Catalog catalog = new Catalog();
                catalog.setSku(columns[0]);
                catalog.setCollection(columns[1]);
                catalog.setName(columns[11]);
                catalog.setTexture(columns[12]);
                catalog.setMaterial(columns[8]);
                catalog.setSize(columns[9]);
                catalog.setSizeAdvance(columns[10]);
                catalog.setUnitOfMeasurement(columns[3]);
                catalog.setQuantityPerBox(parseInteger(columns[4])); // Use helper method
                catalog.setCoverage(parseFloat(columns[5])); // Use helper method
                catalog.setUnitPrice(parseFloat(columns[6])); // Use helper method
                catalog.setWeight(parseFloat(columns[13])); // Use helper method
                catalog.setColor(columns[14]);
                catalog.setCategories(columns[16]);
                catalog.setImages(columns[18]);

                // Add the Catalog object to the list
                catalogList.add(catalog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return catalogList;
    }

    // Create catalog (insert into database from a csv file)
    public ResponseEntity<String> importDataToCatalog() {
        try {
            // Read data from the CSV file
            List<Catalog> catalogList = readCatalogFromCSV("E:\\DaiHoc\\HK6_2024-2025\\T2. IE303 - Cong nghe Java\\Project\\export.csv");

            // Validate the catalogList
            if (catalogList.isEmpty()) {
                return ResponseEntity.status(400).body("No data found in the CSV file.");
            }

            // SQL query to insert data into the catalog table
            String sql = "INSERT INTO catalog (sku, collection, name, texture, material, size, size_advance, unit_of_measurement, quantity_per_box, coverage, unit_price, weight, color, categories, images) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON CONFLICT (sku) DO NOTHING";

            // SQL query to insert data into pricing table
            String sqlPricing = "INSERT INTO pricing (sku, unit_price) VALUES (?, ?) ON CONFLICT (sku) DO UPDATE SET unit_price = ?";

            // Loop through the list of Catalog objects and insert each one into the database
            for (Catalog catalog : catalogList) {
                jdbcTemplate.update(sql,
                        catalog.getSku(),
                        catalog.getCollection(),
                        catalog.getName(),
                        catalog.getTexture(),
                        catalog.getMaterial(),
                        catalog.getSize(),
                        catalog.getSizeAdvance(),
                        catalog.getUnitOfMeasurement(),
                        catalog.getQuantityPerBox(),
                        catalog.getCoverage(),
                        catalog.getUnitPrice(),
                        catalog.getWeight(),
                        catalog.getColor(),
                        catalog.getCategories(),
                        catalog.getImages()
                );
                jdbcTemplate.update(sqlPricing,
                        catalog.getSku(),
                        catalog.getUnitPrice() * 1.3f, // Apply markup of 30% by default for INSERT
                        catalog.getUnitPrice() * 1.3f // Apply markup of 30% by default for UPDATE
                );
            }

            // Return 201 Created if the operation is successful
            return ResponseEntity.status(201).body("Catalog created successfully!");

        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while creating catalog: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body("Error occurred while creating catalog: " + e.getMessage());
        }
    }

    // Get all product in catalogs
    public ResponseEntity<List<Catalog>> getAllProducts() {
        String sql = "SELECT * FROM catalog";
        try {
            List<Catalog> catalogs = jdbcTemplate.query(sql, catalogRowMapper);
            return ResponseEntity.ok(catalogs); // Return 200 OK with the list of catalogs
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching catalogs: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

    // Get product by ID
    public ResponseEntity<Catalog> getProductDetails(String sku) {
        String sql = "SELECT * FROM catalog WHERE sku = ?";
        try {
            // Validate the SKU
            if (sku == null || sku.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // Return 400 Bad Request
            }

            // Execute the query to fetch the catalog by SKU
            List<Catalog> catalogs = jdbcTemplate.query(sql, catalogRowMapper, sku);
            if (catalogs.isEmpty()) {
                return ResponseEntity.status(404).body(null); // Return 404 Not Found
            } else {
                return ResponseEntity.ok(catalogs.get(0)); // Return 200 OK with the catalog
            }
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching catalog by ID: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

}
