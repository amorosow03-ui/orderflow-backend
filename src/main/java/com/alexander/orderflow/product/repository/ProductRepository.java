package com.alexander.orderflow.product.repository;

import com.alexander.orderflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>{
boolean existsBySku(String sku);
}