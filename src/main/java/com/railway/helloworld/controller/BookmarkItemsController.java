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

import com.railway.helloworld.model.TilesModel;
import com.railway.helloworld.repository.BookmarkItemsRepo;

@RestController
@RequestMapping("/api/bookmark")
public class BookmarkItemsController {

    private final BookmarkItemsRepo bookmarkItemsRepo;

    // Constructor injection for bookmarkItemsRepo
    @Autowired
    public BookmarkItemsController(BookmarkItemsRepo bookmarkItemsRepo) {
        this.bookmarkItemsRepo = bookmarkItemsRepo;
    }

    // Get all tiles in a bookmark
    @GetMapping("/{bookmarkId}/items")
    public List<TilesModel> getAllTilessInBookmark(@PathVariable UUID bookmarkId) {
        return bookmarkItemsRepo.getAllTilesInBookmark(bookmarkId).getBody();
    }

    // Add tiles to a bookmark
    @PostMapping("/{bookmarkId}/create")
    public void addTilesTobookmark(@PathVariable UUID bookmarkItemsId, @RequestBody List<Integer> productId) {
        bookmarkItemsRepo.addTilesToBookmark(bookmarkItemsId, productId);
    }

    // Remove tiles from a bookmark
    @PostMapping("/{bookmarkItemsId}/remove")
    public void removeTilesFromBookmark(@PathVariable UUID bookmarkItemsId, @RequestBody List<Integer> productId) {
        bookmarkItemsRepo.removeTilesFromBookmark(bookmarkItemsId, productId);
    }
}
