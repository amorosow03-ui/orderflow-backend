package com.alexander.orderflow.order.dto;

import com.alexander.orderflow.order.entity.Order.OrderStatus;
import com.alexander.orderflow.orderitem.dto.OrderItemSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

public class OrderDetailResponse {

    private Long id;
    private Long customerId;
    private OrderStatus status;
    private List<OrderItemSummaryResponse> items;
    private BigDecimal totalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItemSummaryResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemSummaryResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}