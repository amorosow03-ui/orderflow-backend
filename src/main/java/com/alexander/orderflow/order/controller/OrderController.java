package com.alexander.orderflow.order.controller;

import com.alexander.orderflow.order.dto.OrderDetailResponse;
import com.alexander.orderflow.order.dto.OrderRequest;
import com.alexander.orderflow.order.dto.OrderPatchRequest;
import com.alexander.orderflow.order.dto.OrderResponse;
import com.alexander.orderflow.order.entity.Order;
import com.alexander.orderflow.order.mapper.OrderMapper;
import com.alexander.orderflow.order.service.OrderService;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order API", description = "CRUD operations for Orders")
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    public OrderController(OrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create a new Order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = service.createOrder(request);
        OrderResponse response = mapper.toResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an Order by ID with items and total amount")
    public ResponseEntity<OrderDetailResponse> getOrderById(@PathVariable Long id) {
        Order order = service.getOrderById(id);
        List<OrderItem> orderItems = service.getOrderItemsForOrder(id);

        OrderDetailResponse response = mapper.toDetailResponse(order, orderItems);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all Orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = service.getAllOrders()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all Orders for a Customer")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderResponse> responses = service.getOrdersByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing Order")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request
    ) {
        Order updated = service.updateOrder(id, request);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an Order")
    public ResponseEntity<OrderResponse> patchOrder(
            @PathVariable Long id,
            @RequestBody OrderPatchRequest patchRequest
    ) {
        Order patched = service.patchOrder(id, patchRequest);
        return ResponseEntity.ok(mapper.toResponse(patched));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an Order by ID")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}