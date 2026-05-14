package com.amex.ace.merchant.service;

import com.amex.ace.merchant.dto.MerchantDtos.*;
import com.amex.ace.merchant.entity.Product;
import com.amex.ace.merchant.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Product service — search/filter/categories.
 * Translates Python reference-merchant-backend/app/routes/products.py.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    /** Search products with optional query, category, and price filters. */
    public ProductListResponse searchProducts(String query, String category,
                                              Double minPrice, Double maxPrice,
                                              int limit, int offset) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot))
                  .where(buildPredicates(cb, countRoot, query, category, minPrice, maxPrice));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        // Data query
        CriteriaQuery<Product> dataQuery = cb.createQuery(Product.class);
        Root<Product> root = dataQuery.from(Product.class);
        dataQuery.select(root)
                 .where(buildPredicates(cb, root, query, category, minPrice, maxPrice))
                 .orderBy(cb.asc(root.get("id")));

        List<Product> products = entityManager.createQuery(dataQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        List<ProductResponse> dtos = products.stream().map(this::toDto).toList();
        return new ProductListResponse(dtos, total, limit, offset);
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<Product> root,
                                         String query, String category,
                                         Double minPrice, Double maxPrice) {
        List<Predicate> predicates = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")),        pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
        }
        if (category != null && !category.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("category")),
                    "%" + category.toLowerCase() + "%"));
        }
        if (minPrice != null) predicates.add(cb.ge(root.get("price"), minPrice));
        if (maxPrice != null) predicates.add(cb.le(root.get("price"), maxPrice));
        return predicates.toArray(new Predicate[0]);
    }

    private ProductResponse toDto(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getCategory(), p.getImageUrl(),
                p.getStockQuantity(), p.getCreatedAt());
    }
}
