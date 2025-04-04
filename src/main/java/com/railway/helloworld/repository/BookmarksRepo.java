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

import com.railway.helloworld.model.BookmarksModel;

@Repository
public class BookmarksRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public BookmarksRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Bookmark objects
    private final RowMapper<BookmarksModel> bookmarksRowMapper = (rs, rowNum) -> {
        BookmarksModel bookmark = new BookmarksModel();
        bookmark.setBookmarkId(rs.getObject("bookmark_id", UUID.class));
        bookmark.setBookmarkName(rs.getString("bookmark_name"));
        bookmark.setDateCreated(rs.getDate("created_on"));
        return bookmark;
    };

    // Get all bookmarks
    public ResponseEntity<List<BookmarksModel>> getAllBookmarks() {
        String sql = "SELECT * FROM bookmarks";
        try {
            List<BookmarksModel> bookmarks = jdbcTemplate.query(sql, bookmarksRowMapper);
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
    public ResponseEntity<Optional<BookmarksModel>> gettBookmarkById(String bookmarkId) {
        String sql = "SELECT * FROM bookmarks WHERE bookmark_id = ?";
        try {
            List<BookmarksModel> bookmarks = jdbcTemplate.query(sql, bookmarksRowMapper, bookmarkId);
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
    public ResponseEntity<String> createBookmark(BookmarksModel bookmark) {
        String sql = "INSERT INTO bookmarks (bookmark_id, bookmark_name, created_on) VALUES (?, ?, ?)";
        try {
            // Validation
            // Check if the name is null or empty
            if (bookmark.getBookmarkName() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Return 400 Bad Request
            }

            // Check if the bookmark already exists
            String checkSql = "SELECT COUNT(*) FROM bookmarks WHERE bookmark_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bookmark.getBookmarkId());
            if (count != null && count > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Return 409 Conflict
            }

            // Generate a new UUID if bookmarkId is null
            if (bookmark.getBookmarkId() == null) {
                bookmark.setBookmarkId(UUID.randomUUID()); // Generate a new UUID
            }
            // Insert the new bookmark
            if (bookmark.getDateCreated() == null) {
                bookmark.setDateCreated(new Date()); // Set the current date if DateCreated is null
            }
            // Insert the bookmark into the database
            jdbcTemplate.update(sql, bookmark.getBookmarkId(), bookmark.getBookmarkName(), bookmark.getDateCreated());
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
