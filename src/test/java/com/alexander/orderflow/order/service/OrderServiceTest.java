package com.alexander.orderflow.order.service;

import com.alexander.orderflow.customer.repository.CustomerRepository;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.repository.OrderRepository;
import com.alexander.orderflow.orderitem.repository.OrderItemRepository;
import com.alexander.orderflow.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        orderItemRepository = Mockito.mock(OrderItemRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);

        orderService = new OrderService(
                orderRepository,
                customerRepository,
                null, // mapper brauchen wir hier nicht
                orderItemRepository,
                productRepository
        );
    }

    @Test
    void shouldUpdateStatusFromCreatedToPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        Mockito.when(orderRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.patchOrder(1L, new com.alexander.orderflow.order.dto.OrderPatchRequest() {{
            setStatus(Order.OrderStatus.PAID);
        }});

        assert result.getStatus() == Order.OrderStatus.PAID;
    }

    @Test
    void shouldThrowExceptionForInvalidStatusTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        var patchRequest = new com.alexander.orderflow.order.dto.OrderPatchRequest();
        patchRequest.setStatus(Order.OrderStatus.SHIPPED);

        assertThrows(
                com.alexander.orderflow.exception.InvalidOrderStateException.class,
                () -> orderService.patchOrder(1L, patchRequest)
        );
    }

    @Test
    void shouldRestoreStockWhenOrderCancelled() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        var product = new com.alexander.orderflow.product.entity.Product();
        product.setStockQuantity(5);

        var orderItem = new com.alexander.orderflow.orderitem.entity.OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        Mockito.when(orderItemRepository.findByOrderId(1L))
                .thenReturn(java.util.List.of(orderItem));

        Mockito.when(productRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var patchRequest = new com.alexander.orderflow.order.dto.OrderPatchRequest();
        patchRequest.setStatus(Order.OrderStatus.CANCELLED);

        orderService.patchOrder(1L, patchRequest);

        assert product.getStockQuantity() == 8; // 5 + 3
    }

    @Test
    void shouldThrowExceptionWhenDeletingPaidOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.PAID);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        assertThrows(
                com.alexander.orderflow.exception.InvalidOrderStateException.class,
                () -> orderService.deleteOrder(1L)
        );
    }

    @Test
    void shouldDeleteCreatedOrderAndRestoreStock() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        var product = new com.alexander.orderflow.product.entity.Product();
        product.setStockQuantity(10);

        var item = new com.alexander.orderflow.orderitem.entity.OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        Mockito.when(orderItemRepository.findByOrderId(1L))
                .thenReturn(java.util.List.of(item));

        orderService.deleteOrder(1L);

        assert product.getStockQuantity() == 12;
        Mockito.verify(orderRepository).delete(order);
    }
}