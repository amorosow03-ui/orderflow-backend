package com.alexander.orderflow.product.controller;

import com.alexander.orderflow.product.dto.CreateProductRequest;
import com.alexander.orderflow.product.dto.ProductResponse;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.mapper.ProductMapper;
import com.alexander.orderflow.product.service.ProductService;
import com.alexander.orderflow.product.dto.ProductPatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.alexander.orderflow.product.dto.UpdateProductRequest;
import com.alexander.orderflow.product.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Products", description = "Endpoints for managing products")
@RestController
@RequestMapping("/api/products")
public class ProductController{
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @Operation(summary = "Create a new product")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request){
        Product product = productMapper.toEntity(request);
        Product savedProduct = productService.createProduct(product);
        return productMapper.toResponse(savedProduct);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return productMapper.toResponse(product);
    }

    @Operation(summary = "Get paginated list of products")
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

    @Operation(summary = "Delete product by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update an existing product")
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request){
        Product updatedProduct = productService.updateProduct(id, request);
        return productMapper.toResponse(updatedProduct);
    }

    @Operation(summary = "Search products with optional filters")
    @GetMapping("/search")
    public PagedResponse<ProductResponse> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable
    ) {
        Page<Product> productPage = productService.searchProducts(name, sku, minPrice, maxPrice, pageable);

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

    @Operation(summary = "Partially update an existing product")
    @PatchMapping("/{id}")
    public ProductResponse patchProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductPatchRequest request
    ){
        Product updatedProduct = productService.patchProduct(id, request);
        return productMapper.toResponse(updatedProduct);
    }
}

