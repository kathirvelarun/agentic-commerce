package com.commerce.merchant.service;

import com.commerce.merchant.dto.MerchantDtos.*;
import com.commerce.merchant.model.*;
import com.commerce.merchant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
                String q = query.toLowerCase();
                products = products.stream()
                        .filter(p -> p.getName().toLowerCase().contains(q) ||
                                     (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)))
                        .toList();
            }
        } else if (query != null && !query.isBlank()) {
            products = productRepository.searchProducts(query);
        } else {
            products = productRepository.findAll();
        }
        List<ProductResponse> responses = products.stream().map(this::toResponse).toList();
        return new ProductSearchResponse(responses, responses.size(), query);
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
