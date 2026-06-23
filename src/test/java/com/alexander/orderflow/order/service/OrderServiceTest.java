package com.alexander.orderflow.order.service;

import com.alexander.orderflow.customer.entity.Customer;
import com.alexander.orderflow.customer.repository.CustomerRepository;
import com.alexander.orderflow.exception.InvalidOrderStateException;
import com.alexander.orderflow.exception.ResourceNotFoundException;
import com.alexander.orderflow.order.dto.OrderRequest;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.mapper.OrderMapper;
import com.alexander.orderflow.order.repository.OrderRepository;
import com.alexander.orderflow.orderitem.repository.OrderItemRepository;
import com.alexander.orderflow.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        orderItemRepository = Mockito.mock(OrderItemRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        orderMapper = Mockito.mock(OrderMapper.class);

        orderService = new OrderService(
                orderRepository,
                customerRepository,
                orderMapper,
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

        assertEquals(Order.OrderStatus.PAID, result.getStatus());
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

        assertEquals(8, product.getStockQuantity()); // 5 + 3
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

        assertEquals(12, product.getStockQuantity());
        Mockito.verify(orderRepository).delete(order);
    }

    @Test
    void shouldRejectAnyFieldChangeWhenOrderIsShipped() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.SHIPPED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        // Gleicher Status wird mitgeschickt - kein "echter" Statuswechsel,
        // aber die Order ist trotzdem komplett gesperrt.
        var patchRequest = new com.alexander.orderflow.order.dto.OrderPatchRequest();
        patchRequest.setCustomerId(99L);
        patchRequest.setStatus(Order.OrderStatus.SHIPPED);

        assertThrows(
                com.alexander.orderflow.exception.InvalidOrderStateException.class,
                () -> orderService.patchOrder(1L, patchRequest)
        );

        // Sicherstellen, dass die Order NICHT gespeichert wurde
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldRejectOrderCreationWithNonCreatedStatus() {
        var customer = new com.alexander.orderflow.customer.entity.Customer();
        customer.setId(1L);

        Mockito.when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        var request = new com.alexander.orderflow.order.dto.OrderRequest();
        request.setCustomerId(1L);
        request.setStatus(Order.OrderStatus.SHIPPED);

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.createOrder(request)
        );

        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    // =========================
    // CREATE ORDER - weitere Fälle
    // =========================

    @Test
    void shouldCreateOrderSuccessfullyWhenCustomerExists() {
        var customer = new Customer();
        customer.setId(1L);

        Order mappedOrder = new Order();
        mappedOrder.setCustomer(customer);
        mappedOrder.setStatus(Order.OrderStatus.CREATED);

        var request = new OrderRequest();
        request.setCustomerId(1L);
        // kein Status gesetzt -> Default CREATED greift über die Entity

        Mockito.when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        Mockito.when(orderMapper.toEntity(request, customer))
                .thenReturn(mappedOrder);

        Mockito.when(orderRepository.save(mappedOrder))
                .thenReturn(mappedOrder);

        Order result = orderService.createOrder(request);

        assertEquals(Order.OrderStatus.CREATED, result.getStatus());
        Mockito.verify(orderRepository).save(mappedOrder);
    }

    @Test
    void shouldAllowOrderCreationWithExplicitCreatedStatus() {
        var customer = new Customer();
        customer.setId(1L);

        Order mappedOrder = new Order();
        mappedOrder.setCustomer(customer);
        mappedOrder.setStatus(Order.OrderStatus.CREATED);

        var request = new OrderRequest();
        request.setCustomerId(1L);
        request.setStatus(Order.OrderStatus.CREATED);

        Mockito.when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        Mockito.when(orderMapper.toEntity(request, customer))
                .thenReturn(mappedOrder);

        Mockito.when(orderRepository.save(mappedOrder))
                .thenReturn(mappedOrder);

        Order result = orderService.createOrder(request);

        assertEquals(Order.OrderStatus.CREATED, result.getStatus());
    }

    @Test
    void shouldThrowWhenCreatingOrderForNonExistentCustomer() {
        var request = new OrderRequest();
        request.setCustomerId(404L);

        Mockito.when(customerRepository.findById(404L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    // =========================
    // UPDATE ORDER (PUT) TESTS
    // =========================

    @Test
    void shouldUpdateOrderCustomerAndStatusWhenCreated() {
        var oldCustomer = new Customer();
        oldCustomer.setId(1L);

        var newCustomer = new Customer();
        newCustomer.setId(2L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(oldCustomer);
        order.setStatus(Order.OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        Mockito.when(customerRepository.findById(2L))
                .thenReturn(java.util.Optional.of(newCustomer));

        Mockito.when(orderRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new OrderRequest();
        request.setCustomerId(2L);
        request.setStatus(Order.OrderStatus.PAID);

        Order result = orderService.updateOrder(1L, request);

        assertEquals(2L, result.getCustomer().getId());
        assertEquals(Order.OrderStatus.PAID, result.getStatus());
    }

    @Test
    void shouldRejectUpdateOrderWhenOrderIsShipped() {
        var customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(Order.OrderStatus.SHIPPED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        var request = new OrderRequest();
        request.setCustomerId(99L);
        request.setStatus(Order.OrderStatus.SHIPPED);

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrder(1L, request)
        );

        // Wichtig: Der Customer-Lookup darf gar nicht erst passieren,
        // weil der Guard VOR dem Lookup greift.
        Mockito.verify(customerRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldRejectUpdateOrderWhenOrderIsCancelled() {
        var customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(Order.OrderStatus.CANCELLED);

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(java.util.Optional.of(order));

        var request = new OrderRequest();
        request.setCustomerId(1L);
        request.setStatus(Order.OrderStatus.CANCELLED);

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrder(1L, request)
        );

        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }
}