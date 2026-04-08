package com.alexander.orderflow.orderitem.controller;

import com.alexander.orderflow.orderitem.dto.OrderItemRequest;
import com.alexander.orderflow.orderitem.dto.OrderItemResponse;
import com.alexander.orderflow.orderitem.dto.OrderItemPatchRequest;
import com.alexander.orderflow.orderitem.entity.OrderItem;
import com.alexander.orderflow.orderitem.mapper.OrderItemMapper;
import com.alexander.orderflow.orderitem.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
@Tag(name = "OrderItem API", description = "Operations for Order Items")
public class OrderItemController {

    private final OrderItemService service;
    private final OrderItemMapper mapper;

    public OrderItemController(OrderItemService service, OrderItemMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Add a product to an order")
    public ResponseEntity<OrderItemResponse> createOrderItem(
            @Valid @RequestBody OrderItemRequest request
    ) {
        OrderItem created = service.createOrderItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an OrderItem by ID")
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable Long id) {
        OrderItem orderItem = service.getOrderItemById(id);
        return ResponseEntity.ok(mapper.toResponse(orderItem));
    }

    @GetMapping
    @Operation(summary = "Get all OrderItems")
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems() {
        List<OrderItemResponse> responses = service.getAllOrderItems()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get all OrderItems for an Order")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItemResponse> responses = service.getOrderItemsByOrderId(orderId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an OrderItem by ID")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        service.deleteOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update OrderItem quantity")
    public ResponseEntity<OrderItemResponse> updateOrderItemQuantity(
            @PathVariable Long id,
            @Valid @RequestBody OrderItemPatchRequest request
    ) {
        OrderItem updated = service.updateOrderItemQuantity(id, request);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
}