package com.alexander.orderflow.product.service;

import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.alexander.orderflow.exception.DuplicateSkuException;

@Service
public class ProductService{
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product){
        if(productRepository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + product.getSku() + " already exists.");
        }
        return productRepository.save(product);
    }
}