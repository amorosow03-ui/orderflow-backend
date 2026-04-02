package com.alexander.orderflow.product.service;

import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.repository.ProductRepository;
import com.alexander.orderflow.product.specification.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.alexander.orderflow.exception.DuplicateSkuException;
import com.alexander.orderflow.exception.ProductNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import com.alexander.orderflow.product.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.alexander.orderflow.product.dto.ProductPatchRequest;

@Service
public class ProductService{
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product){
        if(productRepository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + product.getSku() + " already exists");
        }
        return productRepository.save(product);
    }

    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    public Page<Product> getAllProducts(Pageable pageable){
        return productRepository.findAll(pageable);
    }

    public void deleteProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        productRepository.delete(product);
    }

    public Product updateProduct(Long id, UpdateProductRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        if(productRepository.existsBySkuAndIdNot(request.getSku(), id)){
            throw new DuplicateSkuException("Product with SKU " + product.getSku() + " already exists");
        }

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        return productRepository.save(product);
    }

    public Page<Product> searchProducts(
            String name,
            String sku,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Specification<Product> spec = Specification
                .where(ProductSpecification.hasName(name))
                .and(ProductSpecification.hasSku(sku))
                .and(ProductSpecification.priceGreaterThanOrEqualTo(minPrice))
                .and(ProductSpecification.priceLessThanOrEqualTo(maxPrice));

        return productRepository.findAll(spec, pageable);
    }

    public Product patchProduct(Long id, ProductPatchRequest request) {
        Product product = getProductById(id);

        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }

        return productRepository.save(product);
    }
}