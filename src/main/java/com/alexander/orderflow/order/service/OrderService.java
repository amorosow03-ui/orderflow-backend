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

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderMapper = orderMapper;
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

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

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

        if (patchRequest.getStatus() != null) {
            existing.setStatus(patchRequest.getStatus());
        }

        return orderRepository.save(existing);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order existing = getOrderById(id);
        orderRepository.delete(existing);
    }
}