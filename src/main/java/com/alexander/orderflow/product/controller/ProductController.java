package com.alexander.orderflow.product.controller;

import com.alexander.orderflow.product.dto.CreateProductRequest;
import com.alexander.orderflow.product.dto.ProductResponse;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.mapper.ProductMapper;
import com.alexander.orderflow.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/products")
public class ProductController{
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request){
        Product product = productMapper.toEntity(request);
        Product savedProduct = productService.createProduct(product);
        return productMapper.toResponse(savedProduct);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return productMapper.toResponse(product);
    }
}

