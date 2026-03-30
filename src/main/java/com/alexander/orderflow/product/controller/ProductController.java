package com.alexander.orderflow.product.controller;

import com.alexander.orderflow.product.dto.CreateProductRequest;
import com.alexander.orderflow.product.dto.ProductResponse;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.mapper.ProductMapper;
import com.alexander.orderflow.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.stream.Collectors;
import com.alexander.orderflow.product.dto.UpdateProductRequest;

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

    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request){
        Product updatedProduct = productService.updateProduct(id, request);
        return productMapper.toResponse(updatedProduct);
    }
}

