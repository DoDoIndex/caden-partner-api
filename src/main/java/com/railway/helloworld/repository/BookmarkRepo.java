package com.railway.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
        bookmark.setBookmarkId(rs.getString("bookmark_id"));
        bookmark.setBookmarkValue(rs.getString("bookmark_value"));
        return bookmark;
    };

    // Check the bookmark table in the database
    public String checkBookMarkTable() {
        String sql = "SELECT COUNT(*) FROM bookmark";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count > 0 ? "Bookmark table contains " + count + " item(s)" : "Bookmark table is empty";
    }

    // Get all bookmarks
    public List<Bookmark> findAll() {
        String sql = "SELECT * FROM bookmark";
        return jdbcTemplate.query(sql, bookmarkRowMapper);
    }

    // Find bookmark by ID
    public Optional<Bookmark> findById(String bookmarkId) {
        String sql = "SELECT * FROM bookmark WHERE bookmark_id = ?";
        List<Bookmark> bookmarks = jdbcTemplate.query(sql, bookmarkRowMapper, bookmarkId);
        return bookmarks.isEmpty() ? Optional.empty() : Optional.of(bookmarks.get(0));
    }

    // Create a new bookmark
    public void createBookmark(Bookmark bookmark) {
        String sql = "INSERT INTO bookmark (bookmark_id, bookmark_value) VALUES (?, ?)";
        jdbcTemplate.update(sql, bookmark.getBookmarkId(), bookmark.getBookmarkValue());
    }

    // Add an item to the bookmark (assuming a more complex structure is needed)
    public void addItemToBookmark(String bookmarkId, String item) {
        // Logic to add an item to the bookmark group
    }

    // Remove an item from the bookmark
    public void removeItemFromBookmark(String bookmarkId, String item) {
        // Logic to remove an item from the bookmark group
    }
}
