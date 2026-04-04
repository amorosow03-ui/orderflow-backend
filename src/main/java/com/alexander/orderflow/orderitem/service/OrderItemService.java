package com.alexander.orderflow.orderitem.service;

import com.alexander.orderflow.exception.InsufficientStockException;
import com.alexander.orderflow.exception.InvalidOrderStateException;
import com.alexander.orderflow.exception.ResourceNotFoundException;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.repository.OrderRepository;
import com.alexander.orderflow.orderitem.dto.OrderItemRequest;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import com.alexander.orderflow.orderitem.mapper.OrderItemMapper;
import com.alexander.orderflow.orderitem.repository.OrderItemRepository;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository,
                            OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Transactional
    public OrderItem createOrderItem(OrderItemRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + request.getOrderId()));
        if (order.getStatus() != Order.OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "OrderItems can only be added when order status is CREATED. Current status: " + order.getStatus()
            );
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Not enough stock for product id " + product.getId()
                            + ". Available: " + product.getStockQuantity()
                            + ", requested: " + request.getQuantity()
            );
        }

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        OrderItem orderItem = orderItemMapper.toEntity(
                order,
                product,
                request.getQuantity(),
                product.getPrice()
        );

        return orderItemRepository.save(orderItem);
    }

    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderItem not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public void deleteOrderItem(Long id) {
        OrderItem existing = getOrderItemById(id);
        orderItemRepository.delete(existing);
    }
}