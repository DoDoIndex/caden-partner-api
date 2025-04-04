package com.railway.helloworld.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.Bookmark;

@Repository
public class BookmarkRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public BookmarkRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Bookmark objects
    private final RowMapper<Bookmark> bookmarkRowMapper = (rs, rowNum) -> {
        Bookmark bookmark = new Bookmark();
        bookmark.setBookmarkId(rs.getObject("bookmark_id", UUID.class));
        bookmark.setName(rs.getString("name"));
        bookmark.setCreatedOn(rs.getDate("created_on"));
        return bookmark;
    };

    // Get all bookmarks
    public ResponseEntity<List<Bookmark>> getAllBookmarks() {
        String sql = "SELECT * FROM bookmark";
        try {
            List<Bookmark> bookmarks = jdbcTemplate.query(sql, bookmarkRowMapper);
            return ResponseEntity.ok(bookmarks); // Return 200 OK with the list of bookmarks
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching bookmarks: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Find bookmark by ID 
    // Sử dụng Optional vì có thể không tìm thấy bookmark với ID đó
    // Optional giúp tránh NullPointerException
    public ResponseEntity<Optional<Bookmark>> gettBookmarkById(String bookmarkId) {
        String sql = "SELECT * FROM bookmark WHERE bookmark_id = ?";
        try {
            List<Bookmark> bookmarks = jdbcTemplate.query(sql, bookmarkRowMapper, bookmarkId);
            if (bookmarks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 Not Found
            } else {
                return ResponseEntity.ok(Optional.of(bookmarks.get(0))); // Return 200 OK with the bookmark
            }
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while fetching bookmark by ID: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Create a new bookmark group
    public ResponseEntity createBookmark(Bookmark bookmark) {
        String sql = "INSERT INTO bookmark (bookmark_id, name, created_on) VALUES (?, ?, ?)";
        try {
            // Validation
            // Check if the bookmark ID is valid (UUID format)
            // Check if the name is null or empty
            if (bookmark.getBookmarkId().toString().length() != 36 || bookmark.getName() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Return 400 Bad Request
            }

            // Check if the bookmark already exists
            String checkSql = "SELECT COUNT(*) FROM bookmark WHERE bookmark_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bookmark.getBookmarkId());
            if (count != null && count > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Return 409 Conflict
            }

            // Insert the new bookmark
            if (bookmark.getCreatedOn() == null) {
                bookmark.setCreatedOn(new Date()); // Set the current date if createdOn is null
            }
            jdbcTemplate.update(sql, bookmark.getBookmarkId(), bookmark.getName(), bookmark.getCreatedOn());
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Return 201 Created
        } catch (Exception e) {
            // Log the error (optional)
            System.err.println("Error occurred while creating bookmark: " + e.getMessage());
            e.printStackTrace();

            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
