package com.alexander.orderflow.customer.controller;

import com.alexander.orderflow.customer.dto.CustomerRequest;
import com.alexander.orderflow.customer.dto.CustomerResponse;
import com.alexander.orderflow.customer.dto.CustomerPatchRequest;
import com.alexander.orderflow.customer.entity.Customer;
import com.alexander.orderflow.customer.mapper.CustomerMapper;
import com.alexander.orderflow.customer.service.CustomerService;
import com.alexander.orderflow.product.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "CRUD operations for Customers")
public class CustomerController {

    private final CustomerService service;
    private final CustomerMapper mapper;

    public CustomerController(CustomerService service, CustomerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create a new Customer")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request
    ) {
        Customer customer = service.createCustomer(request);
        CustomerResponse response = mapper.toResponse(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a Customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        Customer customer = service.getCustomerById(id);
        CustomerResponse response = mapper.toResponse(customer);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of customers")
    public ResponseEntity<PagedResponse<CustomerResponse>> getAllCustomers(
            @PageableDefault(page = 0, size = 10, sort = "lastName") Pageable pageable
    ) {
        Page<Customer> customerPage = service.getAllCustomers(pageable);

        List<CustomerResponse> content = customerPage.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        PagedResponse<CustomerResponse> response = new PagedResponse<>(
                content,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing Customer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        Customer updated = service.updateCustomer(id, request);
        CustomerResponse response = mapper.toResponse(updated);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a Customer")
    public ResponseEntity<CustomerResponse> patchCustomer(
            @PathVariable Long id,
            @RequestBody CustomerPatchRequest patchRequest
    ) {
        Customer patched = service.patchCustomer(id, patchRequest);
        CustomerResponse response = mapper.toResponse(patched);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Customer by ID")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}