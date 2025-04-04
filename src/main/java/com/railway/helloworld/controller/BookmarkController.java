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

    // Get all bookmarks
    @GetMapping
    public List<Bookmark> getAllBookmarks() {
        return bookmarkRepo.getAllBookmarks().getBody();
    }

    // Get bookmark by ID
    @GetMapping("/{bookmarkId}")
    public Bookmark getBookmarkById(@PathVariable String bookmarkId) {
        return bookmarkRepo.gettBookmarkById(bookmarkId).getBody().orElse(null);
    }

    // Create a new bookmark
    @PostMapping("/create")
    public void createBookmark(@RequestBody Bookmark bookmark) {
        bookmarkRepo.createBookmark(bookmark);
    }
}
