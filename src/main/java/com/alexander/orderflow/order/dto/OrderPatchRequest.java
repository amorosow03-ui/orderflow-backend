package com.alexander.orderflow.order.dto;

import com.alexander.orderflow.order.entity.Order.OrderStatus;

public class OrderPatchRequest {

    private Long customerId;
    private OrderStatus status;

    // Getter & Setter
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}