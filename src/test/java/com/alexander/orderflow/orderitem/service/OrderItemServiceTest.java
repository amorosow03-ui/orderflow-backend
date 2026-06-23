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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(7, product.getStockQuantity());
        Mockito.verify(orderItemRepository).save(orderItem);
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

        // 1. State prüfen
        assertEquals(8, product.getStockQuantity());

        // 2. Behavior prüfen
        Mockito.verify(productRepository).save(product);
        Mockito.verify(orderItemRepository).delete(orderItem);
    }

    @Test
    void shouldThrowWhenDeletingOrderItemNotCreated() {
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PAID);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        assertThrows(InvalidOrderStateException.class, () ->
                orderItemService.deleteOrderItem(1L)
        );

        // WICHTIG: nichts darf passieren
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(orderItemRepository, Mockito.never()).delete(Mockito.any());
    }

    // =========================
    // UPDATE ORDER ITEM QUANTITY TESTS
    // =========================

    @Test
    void shouldReduceStockWhenQuantityIncreasedWithEnoughStock() {
        // GIVEN: Bestellmenge soll von 3 auf 5 erhöht werden (+2),
        // Produkt hat noch 10 auf Lager.
        Order order = new Order();
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(10);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        var patchRequest = new com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest();
        patchRequest.setQuantity(5);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        Mockito.when(orderItemRepository.save(orderItem))
                .thenReturn(orderItem);

        // WHEN
        OrderItem result = orderItemService.updateOrderItemQuantity(1L, patchRequest);

        // THEN
        // Stock muss um die Differenz (2) sinken: 10 - 2 = 8
        assertEquals(8, product.getStockQuantity());
        assertEquals(5, result.getQuantity());
        Mockito.verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenIncreasingQuantityBeyondAvailableStock() {
        // GIVEN: Bestellmenge soll von 3 auf 10 erhöht werden (+7),
        // Produkt hat aber nur noch 2 auf Lager.
        Order order = new Order();
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(2);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        var patchRequest = new com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest();
        patchRequest.setQuantity(10);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        // THEN
        assertThrows(InsufficientStockException.class, () ->
                orderItemService.updateOrderItemQuantity(1L, patchRequest)
        );

        // Stock darf NICHT verändert worden sein, weil die Operation
        // komplett abgelehnt wird, nicht teilweise ausgeführt.
        assertEquals(2, product.getStockQuantity());
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(orderItemRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldRestoreStockWhenQuantityDecreased() {
        // GIVEN: Bestellmenge soll von 5 auf 2 verringert werden (-3),
        // die Differenz von 3 muss an den Lagerbestand zurückgegeben werden.
        Order order = new Order();
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(4);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(5);

        var patchRequest = new com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest();
        patchRequest.setQuantity(2);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        Mockito.when(orderItemRepository.save(orderItem))
                .thenReturn(orderItem);

        // WHEN
        OrderItem result = orderItemService.updateOrderItemQuantity(1L, patchRequest);

        // THEN
        // Stock muss um die Differenz (3) steigen: 4 + 3 = 7
        assertEquals(7, product.getStockQuantity());
        assertEquals(2, result.getQuantity());
        Mockito.verify(productRepository).save(product);
    }

    @Test
    void shouldNotTouchStockWhenQuantityUnchanged() {
        // GIVEN: Bestellmenge bleibt gleich (5 -> 5), Stock darf gar nicht
        // berührt werden.
        Order order = new Order();
        order.setStatus(Order.OrderStatus.CREATED);

        Product product = new Product();
        product.setStockQuantity(4);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(5);

        var patchRequest = new com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest();
        patchRequest.setQuantity(5);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        Mockito.when(orderItemRepository.save(orderItem))
                .thenReturn(orderItem);

        // WHEN
        orderItemService.updateOrderItemQuantity(1L, patchRequest);

        // THEN
        assertEquals(4, product.getStockQuantity());
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowWhenUpdatingQuantityOnNonCreatedOrder() {
        // GIVEN: Order ist bereits PAID, Menge darf nicht mehr verändert werden.
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PAID);

        Product product = new Product();
        product.setStockQuantity(10);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        var patchRequest = new com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest();
        patchRequest.setQuantity(5);

        Mockito.when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(orderItem));

        // THEN
        assertThrows(InvalidOrderStateException.class, () ->
                orderItemService.updateOrderItemQuantity(1L, patchRequest)
        );

        // Nichts darf angefasst worden sein
        assertEquals(10, product.getStockQuantity());
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(orderItemRepository, Mockito.never()).save(Mockito.any());
    }
}