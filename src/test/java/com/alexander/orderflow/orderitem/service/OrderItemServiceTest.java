package com.alexander.orderflow.orderitem.service;

import com.alexander.orderflow.exception.InsufficientStockException;
import com.alexander.orderflow.exception.InvalidOrderStateException;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.repository.OrderRepository;
import com.alexander.orderflow.orderitem.dto.OrderItemRequest;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import com.alexander.orderflow.orderitem.mapper.OrderItemMapper;
import com.alexander.orderflow.orderitem.repository.OrderItemRepository;
import com.alexander.orderflow.product.entity.Product;
import com.alexander.orderflow.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService orderItemService;

    // =========================
    // CREATE ORDER ITEM TESTS
    // =========================

    @Test
    void shouldCreateOrderItemAndReduceStock() {
        // GIVEN
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(10);

        OrderItemRequest request = new OrderItemRequest();
        request.setOrderId(1L);
        request.setProductId(2L);
        request.setQuantity(3);

        OrderItem orderItem = new OrderItem();

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Mockito.when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        Mockito.when(orderItemMapper.toEntity(order, product, 3, product.getPrice()))
                .thenReturn(orderItem);

        Mockito.when(orderItemRepository.save(orderItem))
                .thenReturn(orderItem);

        // WHEN
        orderItemService.createOrderItem(request);

        // THEN
        assert product.getStockQuantity() == 7;
    }

    @Test
    void shouldThrowWhenOrderNotCreated() {
        // GIVEN
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.PAID);

        OrderItemRequest request = new OrderItemRequest();
        request.setOrderId(1L);
        request.setProductId(2L);
        request.setQuantity(1);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // THEN
        assertThrows(InvalidOrderStateException.class, () ->
                orderItemService.createOrderItem(request)
        );
    }

    @Test
    void shouldThrowWhenInsufficientStock() {
        // GIVEN
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(2);

        OrderItemRequest request = new OrderItemRequest();
        request.setOrderId(1L);
        request.setProductId(2L);
        request.setQuantity(5);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Mockito.when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        // THEN
        assertThrows(InsufficientStockException.class, () ->
                orderItemService.createOrderItem(request)
        );
    }

    // =========================
    // DELETE ORDER ITEM TESTS
    // =========================

    @Test
    void shouldDeleteOrderItemAndRestoreStock() {
        // GIVEN
        Order order = new Order();
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(5);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        // WHEN
        orderItemService.deleteOrderItem(1L);

        // THEN
        assert product.getStockQuantity() == 8;
    }

    @Test
    void shouldThrowWhenDeletingOrderItemNotCreated() {
        // GIVEN
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PAID);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        // THEN
        assertThrows(InvalidOrderStateException.class, () ->
                orderItemService.deleteOrderItem(1L)
        );
    }
}