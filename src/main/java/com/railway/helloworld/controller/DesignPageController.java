package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.helloworld.model.DesignPageModel;

@RestController
@RequestMapping("/api/design-pages")
public class DesignPageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<?> createDesignPage(@RequestBody DesignPageModel designPage) {
        try {
            String sql = "INSERT INTO Design_Page (phone, company_name, email, bookmark, collection) VALUES (?, ?, ?, ?::jsonb, ?::jsonb)";

            jdbcTemplate.update(sql,
                    designPage.getPhone(),
                    designPage.getCompanyName(),
                    designPage.getEmail(),
                    designPage.getBookmark() != null ? designPage.getBookmark().toString() : "[]",
                    designPage.getCollection() != null ? designPage.getCollection().toString() : "[]"
            );

            return ResponseEntity.ok().body("Design page created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating design page: " + e.getMessage());
        }
    }

    @PutMapping("/{phone}/bookmark")
    public ResponseEntity<?> updateBookmark(
            @PathVariable String phone,
            @RequestBody JsonNode bookmark) {
        try {
            String sql = "UPDATE Design_Page SET bookmark = ?::jsonb WHERE phone = ?";
            jdbcTemplate.update(sql, bookmark.toString(), phone);
            return ResponseEntity.ok().body("Bookmark updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating bookmark: " + e.getMessage());
        }
    }

    @PutMapping("/{phone}/collection")
    public ResponseEntity<?> updateCollection(
            @PathVariable String phone,
            @RequestBody JsonNode collection) {
        try {
            String sql = "UPDATE Design_Page SET collection = ?::jsonb WHERE phone = ?";
            jdbcTemplate.update(sql, collection.toString(), phone);
            return ResponseEntity.ok().body("Collection updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating collection: " + e.getMessage());
        }
    }

    @GetMapping("/{phone}")
    public ResponseEntity<?> getDesignPage(@PathVariable String phone) {
        try {
            String sql = "SELECT * FROM Design_Page WHERE phone = ?";
            List<DesignPageModel> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
                DesignPageModel page = new DesignPageModel();
                page.setPhone(rs.getString("phone"));
                page.setCompanyName(rs.getString("company_name"));
                page.setEmail(rs.getString("email"));
                try {
                    page.setBookmark(objectMapper.readValue(rs.getString("bookmark"),
                            new com.fasterxml.jackson.core.type.TypeReference<List<DesignPageModel.BookmarkItem>>() {
                    }));
                    page.setCollection(objectMapper.readTree(rs.getString("collection")));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parsing JSON data", e);
                }
                return page;
            }, phone);

            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(results.get(0));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving design page: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDesignPages() {
        try {
            String sql = "SELECT * FROM Design_Page";
            List<DesignPageModel> designPages = jdbcTemplate.query(sql, (rs, rowNum) -> {
                DesignPageModel page = new DesignPageModel();
                page.setPhone(rs.getString("phone"));
                page.setCompanyName(rs.getString("company_name"));
                page.setEmail(rs.getString("email"));
                try {
                    page.setBookmark(objectMapper.readValue(rs.getString("bookmark"),
                            new com.fasterxml.jackson.core.type.TypeReference<List<DesignPageModel.BookmarkItem>>() {
                    }));
                    page.setCollection(objectMapper.readTree(rs.getString("collection")));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parsing JSON data", e);
                }
                return page;
            });

            return ResponseEntity.ok(designPages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all design pages: " + e.getMessage());
        }
    }
}
