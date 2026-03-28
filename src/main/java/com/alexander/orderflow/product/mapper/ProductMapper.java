package com.alexander.orderflow.product.mapper;

import com.alexander.orderflow.product.dto.ProductResponse;
import com.alexander.orderflow.product.dto.CreateProductRequest;
import com.alexander.orderflow.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper{

    public Product toEntity(CreateProductRequest request){
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        return product;
    }

    public ProductResponse toResponse(Product product){
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}