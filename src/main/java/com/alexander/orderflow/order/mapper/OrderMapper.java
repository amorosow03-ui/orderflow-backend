package com.alexander.orderflow.order.mapper;

import com.alexander.orderflow.order.dto.OrderDetailResponse;
import com.alexander.orderflow.order.dto.OrderRequest;
import com.alexander.orderflow.order.dto.OrderResponse;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.customer.entity.Customer;
import com.alexander.orderflow.orderitem.dto.OrderItemSummaryResponse;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public OrderDetailResponse toDetailResponse(Order order, List<OrderItem> orderItems) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomer().getId());
        response.setStatus(order.getStatus());

        List<OrderItemSummaryResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItem orderItem : orderItems) {
            OrderItemSummaryResponse itemResponse = new OrderItemSummaryResponse();
            itemResponse.setId(orderItem.getId());
            itemResponse.setProductId(orderItem.getProduct().getId());
            itemResponse.setProductName(orderItem.getProduct().getName());
            itemResponse.setQuantity(orderItem.getQuantity());
            itemResponse.setPriceAtOrder(orderItem.getUnitPrice());

            BigDecimal lineTotal = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

            itemResponse.setLineTotal(lineTotal);

            itemResponses.add(itemResponse);
            totalAmount = totalAmount.add(lineTotal);
        }

        response.setItems(itemResponses);
        response.setTotalAmount(totalAmount);

        return response;
    }
}