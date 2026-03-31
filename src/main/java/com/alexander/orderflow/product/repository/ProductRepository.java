package com.alexander.orderflow.product.repository;

import com.alexander.orderflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
boolean existsBySku(String sku);
boolean existsBySkuAndIdNot(String sku, Long id);
}