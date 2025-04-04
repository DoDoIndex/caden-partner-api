package com.railway.helloworld.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.helloworld.model.Catalog;
import com.railway.helloworld.repository.BookmarkGroupRepo;

@RestController
@RequestMapping("/api/bookmark")
public class BookmarkGroupController {

    private final BookmarkGroupRepo bookmarkGroupRepo;

    // Constructor injection for BookmarkGroupRepo
    @Autowired
    public BookmarkGroupController(BookmarkGroupRepo bookmarkGroupRepo) {
        this.bookmarkGroupRepo = bookmarkGroupRepo;
    }

    // Get all products in bookmark group
    @GetMapping("/{bookmarkGroupId}/items")
    public List<Catalog> getAllProductsInBookmarkGroup(@PathVariable UUID bookmarkGroupId) {
        return bookmarkGroupRepo.getAllProductsInBookmarkGroup(bookmarkGroupId).getBody();
    }

    // Create a new bookmark group
    @PostMapping("/{bookmarkGroupId}/create")
    public void addProductToBookmarkGroup(@PathVariable UUID bookmarkGroupId, @RequestBody String sku) {
        bookmarkGroupRepo.addProductToBookmarkGroup(bookmarkGroupId, sku);
    }

    // Remove a product from bookmark group
    @PostMapping("/{bookmarkGroupId}/remove")
    public void removeProductFromBookmarkGroup(@PathVariable UUID bookmarkGroupId, @RequestBody String sku) {
        bookmarkGroupRepo.removeProductFromBookmarkGroup(bookmarkGroupId, sku);
    }
}
