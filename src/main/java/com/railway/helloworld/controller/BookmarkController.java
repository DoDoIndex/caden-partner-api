package com.railway.helloworld.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.BookmarksModel;
import com.railway.helloworld.repository.BookmarksRepo;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarksRepo bookmarkRepo;

    // Constructor injection for BookmarkRepo
    @Autowired
    public BookmarkController(BookmarksRepo bookmarkRepo) {
        this.bookmarkRepo = bookmarkRepo;
    }

    // Get all bookmarks
    @GetMapping
    public ResponseEntity<List<BookmarksModel>> getAllBookmarks() {
        return bookmarkRepo.getAllBookmarks();
    }

    // Get bookmark by ID
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<Optional<BookmarksModel>> getBookmarkById(@PathVariable UUID bookmarkId) {
        return bookmarkRepo.gettBookmarkById(bookmarkId);
    }

    // Create a new bookmark
    @PostMapping("/create")
    public ResponseEntity<String> createBookmark(@RequestBody BookmarksModel bookmark) {
        return bookmarkRepo.createBookmark(bookmark);
    }
}
