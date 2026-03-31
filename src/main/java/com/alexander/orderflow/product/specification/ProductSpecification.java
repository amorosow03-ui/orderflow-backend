package com.alexander.orderflow.product.specification;

import com.alexander.orderflow.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                name == null || name.isBlank()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Product> hasSku(String sku) {
        return (root, query, criteriaBuilder) ->
                sku == null || sku.isBlank()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")),
                        "%" + sku.toLowerCase() + "%"
                );
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                minPrice == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                maxPrice == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}