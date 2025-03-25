package com.railway.helloworld;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/check-db")
    public String checkDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return "Database is working";
        } catch (Exception e) {
            return "Database is not working: " + e.getMessage();
        }
    }

    @GetMapping("/catalog/sync")
    public String syncCatalog() {
        // Fetch data from /api/sitemap on Caden Tile backend
        // Map partner’s custom pricing to each product_id
        return "Catalog sync completed";
    }

    @PostMapping("/catalog/price")
    public String updatePrice(@RequestBody PriceUpdateRequest request) {
        // Update partner-specific markup for a given product
        return "Price updated";
    }

    @GetMapping("/product/details")
    public String getProductDetails(@RequestParam String product_id) {
        // Pull from /api/product-info
        // Add partner's price
        return "Product details with pricing";
    }

    @PostMapping("/bookmarks/create")
    public String createBookmarkGroup() {
        // Create a new bookmark group
        // Return a sharable link or group ID
        return "Bookmark group created";
    }

    @GetMapping("/bookmarks/{group_id}/items")
    public String listBookmarkItems(@PathVariable String group_id) {
        // List all items in a bookmark group
        return "List of bookmark items";
    }

    @PostMapping("/bookmarks/{group_id}/add")
    public String addBookmarkItem(@PathVariable String group_id, @RequestBody BookmarkItemRequest request) {
        // Add item to bookmark group
        return "Item added to bookmark group";
    }

    @PostMapping("/bookmarks/{group_id}/remove")
    public String removeBookmarkItem(@PathVariable String group_id, @RequestBody BookmarkItemRequest request) {
        // Remove item from group
        return "Item removed from bookmark group";
    }
}

class PriceUpdateRequest {

    public String product_id;
    public double price_markup_percent;
}

class BookmarkItemRequest {

    public String product_id;
}
