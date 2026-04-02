package com.alexander.orderflow.customer.mapper;

import com.alexander.orderflow.customer.dto.CustomerRequest;
import com.alexander.orderflow.customer.dto.CustomerResponse;
import com.alexander.orderflow.customer.dto.CustomerPatchRequest;
import com.alexander.orderflow.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        return customer;
    }

    public void patchEntity(CustomerPatchRequest patch, Customer customer) {
        if (patch.getFirstName() != null) customer.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) customer.setLastName(patch.getLastName());
        if (patch.getEmail() != null) customer.setEmail(patch.getEmail());
        if (patch.getPhoneNumber() != null) customer.setPhoneNumber(patch.getPhoneNumber());
    }
}