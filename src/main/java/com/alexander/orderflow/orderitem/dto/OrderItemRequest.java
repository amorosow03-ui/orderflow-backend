package com.alexander.orderflow.orderitem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public OrderItemRequest() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}