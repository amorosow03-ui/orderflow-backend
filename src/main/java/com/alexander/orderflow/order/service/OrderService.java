package com.alexander.orderflow.order.service;

import com.alexander.orderflow.customer.repository.CustomerRepository;
import com.alexander.orderflow.exception.ResourceNotFoundException;
import com.alexander.orderflow.order.dto.OrderRequest;
import com.alexander.orderflow.order.dto.OrderPatchRequest;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.entity.Order.OrderStatus;
import com.alexander.orderflow.order.mapper.OrderMapper;
import com.alexander.orderflow.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alexander.orderflow.exception.InvalidOrderStateException;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import com.alexander.orderflow.orderitem.repository.OrderItemRepository;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.repository.ProductRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        OrderMapper orderMapper,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderMapper = orderMapper;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        // Customer prüfen
        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id " + request.getCustomerId()));

        Order order = orderMapper.toEntity(request, customer);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Order updateOrder(Long id, OrderRequest request) {
        Order existing = getOrderById(id);

        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id " + request.getCustomerId()));

        existing.setCustomer(customer);

        applyStatusChange(existing, request.getStatus());

        return orderRepository.save(existing);
    }

    @Transactional
    public Order patchOrder(Long id, OrderPatchRequest patchRequest) {
        Order existing = getOrderById(id);

        if (patchRequest.getCustomerId() != null) {
            var customer = customerRepository.findById(patchRequest.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with id " + patchRequest.getCustomerId()));
            existing.setCustomer(customer);
        }

        applyStatusChange(existing, patchRequest.getStatus());

        return orderRepository.save(existing);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order existing = getOrderById(id);
        orderRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsForOrder(Long orderId) {
        getOrderById(orderId);

        return orderItemRepository.findByOrderId(orderId);
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        if (newStatus == null || currentStatus == newStatus) {
            return;
        }

        boolean validTransition = false;

        switch (currentStatus) {
            case CREATED:
                validTransition = (newStatus == Order.OrderStatus.PAID || newStatus == Order.OrderStatus.CANCELLED);
                break;

            case PAID:
                validTransition = (newStatus == Order.OrderStatus.SHIPPED || newStatus == Order.OrderStatus.CANCELLED);
                break;

            case SHIPPED:
            case CANCELLED:
                validTransition = false;
                break;
        }
        if (!validTransition) {
            throw new InvalidOrderStateException(
                    "Invalid order status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

        private void restoreStockForCancelledOrder(Order order) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

            for (OrderItem orderItem : orderItems) {
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }
        }

        private void applyStatusChange(Order order, Order.OrderStatus newStatus) {
            if (newStatus == null || order.getStatus() == newStatus) {
                return;
            }

            validateStatusTransition(order.getStatus(), newStatus);

            // Wenn auf CANCELLED gewechselt wird, Bestand zurückgeben
            if (newStatus == Order.OrderStatus.CANCELLED) {
                restoreStockForCancelledOrder(order);
            }

            order.setStatus(newStatus);
        }
}