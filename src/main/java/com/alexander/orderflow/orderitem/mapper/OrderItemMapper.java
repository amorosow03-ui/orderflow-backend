package com.alexander.orderflow.orderitem.mapper;

import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.orderitem.dto.OrderItemResponse;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import com.alexander.orderflow.product.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);
        return orderItem;
    }

    public OrderItemResponse toResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setOrderId(orderItem.getOrder().getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());

        BigDecimal lineTotal = orderItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        response.setLineTotal(lineTotal);

        return response;
    }
}