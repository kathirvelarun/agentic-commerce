package com.amex.ace.merchant.controller;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Product endpoints — GET /api/products/*, GET /api/products/categories.
 * Maps Python reference-merchant-backend/app/routes/products.py.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** GET /api/products/?query=&category=&min_price=&max_price=&limit=&offset= */
    @GetMapping("/")
    public ProductListResponse searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0")  int offset) {
        return productService.searchProducts(query, category, minPrice, maxPrice, limit, offset);
    }

    /** GET /api/products/categories */
    @GetMapping("/categories")
    public Map<String, List<String>> getCategories() {
        return Map.of("categories", productService.getCategories());
    }

    /** GET /api/products/{id} */
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }
}
