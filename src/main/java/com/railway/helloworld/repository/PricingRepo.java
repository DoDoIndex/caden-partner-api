package com.railway.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.railway.helloworld.model.Pricing;

@Repository
public class PricingRepo {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection for JdbcTemplate
    @Autowired
    public PricingRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Pricing objects
    private final RowMapper<Pricing> pricingRowMapper = (rs, rowNum) -> {
        Pricing pricing = new Pricing();
        pricing.setProductId(rs.getInt("product_id"));
        pricing.setUnitPrice(rs.getDouble("unit_price"));
        return pricing;
    };

    // Check the pricing table in the database
    public String checkPricingTable() {
        String sql = "SELECT COUNT(*) FROM pricing";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count > 0 ? "Pricing table contains " + count + " item(s)" : "Pricing table is empty";
    }

    // Get all pricing data
    public List<Pricing> findAll() {
        String sql = "SELECT * FROM pricing";
        return jdbcTemplate.query(sql, pricingRowMapper);
    }

    // Find pricing by product ID
    public Optional<Pricing> findById(Integer productId) {
        String sql = "SELECT * FROM pricing WHERE product_id = ?";
        List<Pricing> pricingList = jdbcTemplate.query(sql, pricingRowMapper, productId);
        return pricingList.isEmpty() ? Optional.empty() : Optional.of(pricingList.get(0));
    }

    // Update price for a given product
    public void updatePrice(Integer productId, Double newPrice) {
        String sql = "UPDATE pricing SET unit_price = ? WHERE product_id = ?";
        jdbcTemplate.update(sql, newPrice, productId);
    }

    // Create or update product pricing
    public void save(Pricing pricing) {
        String sql = "INSERT INTO pricing (product_id, unit_price) VALUES (?, ?) ON CONFLICT (product_id) DO UPDATE SET unit_price = ?";
        jdbcTemplate.update(sql, pricing.getProductId(), pricing.getUnitPrice(), pricing.getUnitPrice());
    }

}
