package com.commerce.merchant.service;

import com.commerce.merchant.dto.MerchantDtos.*;
import com.commerce.merchant.model.*;
import com.commerce.merchant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse getProduct(String id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public ProductSearchResponse searchProducts(String query, String category) {
        List<Product> products;
        if (category != null && !category.isBlank()) {
            products = productRepository.findByCategoryIgnoreCase(category);
            if (query != null && !query.isBlank()) {
                String[] words = query.toLowerCase().split("\\s+");
                products = products.stream()
                        .filter(p -> Arrays.stream(words).anyMatch(w -> matchesProduct(p, w)))
                        .toList();
            }
        } else if (query != null && !query.isBlank()) {
            products = searchByWords(query);
        } else {
            products = productRepository.findAll();
        }
        List<ProductResponse> responses = products.stream().map(this::toResponse).toList();
        return new ProductSearchResponse(responses, responses.size(), query);
    }

    private List<Product> searchByWords(String query) {
        String[] words = query.toLowerCase().split("\\s+");
        if (words.length == 1) {
            return productRepository.searchProducts(words[0]);
        }
        // Multi-word: union of per-word results, preserving order, deduped by id
        Set<String> seen = new LinkedHashSet<>();
        return Arrays.stream(words)
                .flatMap(word -> productRepository.searchProducts(word).stream())
                .filter(p -> seen.add(p.getId()))
                .toList();
    }

    private boolean matchesProduct(Product p, String word) {
        return (p.getName() != null && p.getName().toLowerCase().contains(word)) ||
               (p.getDescription() != null && p.getDescription().toLowerCase().contains(word)) ||
               (p.getCategory() != null && p.getCategory().toLowerCase().contains(word)) ||
               (p.getBrand() != null && p.getBrand().toLowerCase().contains(word)) ||
               (p.getTags() != null && p.getTags().toLowerCase().contains(word));
    }

    public List<String> getCategories() {
        return productRepository.findAllCategories();
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .imageUrl(request.imageUrl())
                .stock(request.stock())
                .brand(request.brand())
                .tags(request.tags())
                .build();
        return toResponse(productRepository.save(product));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getCategory(), p.getImageUrl(), p.getStock(), p.getRating(),
                p.getBrand(), p.getTags()
        );
    }
}
