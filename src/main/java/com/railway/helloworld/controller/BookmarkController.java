package com.railway.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.Bookmark;
import com.railway.helloworld.repository.BookmarkRepo;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkRepo bookmarkRepo;

    // Constructor injection for BookmarkRepo
    @Autowired
    public BookmarkController(BookmarkRepo bookmarkRepo) {
        this.bookmarkRepo = bookmarkRepo;
    }

    // Check the bookmark table in the database
    @GetMapping("/check-bookmark-table")
    public String checkBookmarkTable() {
        return bookmarkRepo.checkBookMarkTable();
    }

    // Get all bookmarks
    @GetMapping
    public List<Bookmark> getAllBookmarks() {
        return bookmarkRepo.findAll();
    }

    // Create a new bookmark group
    @PostMapping("/create")
    public void createBookmark(@RequestBody Bookmark bookmark) {
        bookmarkRepo.createBookmark(bookmark);
    }

    // List all items in a bookmark group
    @GetMapping("/{group_id}/items")
    public Bookmark getBookmarkById(@PathVariable String groupId) {
        return bookmarkRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Bookmark not found"));
    }

    // Add items from a bookmark group
    @PostMapping("/{group_id}/add")
    public String addBookmarkItem(@PathVariable String group_id, @RequestBody BookmarkItemRequest request) {
        // Add item to bookmark group
        return "Item added to bookmark group";
    }

    // Remove items from a bookmark group
    @PostMapping("/bookmarks/{group_id}/remove")
    public String removeBookmarkItem(@PathVariable String group_id, @RequestBody BookmarkItemRequest request) {
        // Remove item from group
        return "Item removed from bookmark group";
    }
}
