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
import com.alexander.orderflow.product.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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
    public PagedResponse<ProductResponse> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable
    ) {
        Page<Product> productPage = productService.getAllProducts(pageable);

        List<ProductResponse> content = productPage.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
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

