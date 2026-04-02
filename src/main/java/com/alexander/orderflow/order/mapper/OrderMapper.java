package com.alexander.orderflow.order.mapper;

import com.alexander.orderflow.order.dto.OrderRequest;
import com.alexander.orderflow.order.dto.OrderResponse;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomer().getId());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    public Order toEntity(OrderRequest request, Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        return order;
    }
}