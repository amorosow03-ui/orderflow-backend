package com.alexander.orderflow.customer.service;

import com.alexander.orderflow.customer.dto.CustomerPatchRequest;
import com.alexander.orderflow.customer.dto.CustomerRequest;
import com.alexander.orderflow.customer.entity.Customer;
import com.alexander.orderflow.customer.mapper.CustomerMapper;
import com.alexander.orderflow.customer.repository.CustomerRepository;
import com.alexander.orderflow.exception.DuplicateResourceException;
import com.alexander.orderflow.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        // Duplicate Email prüfen
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = mapper.toEntity(request);
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer existing = getCustomerById(id);

        if (!existing.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
        }

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhoneNumber(request.getPhoneNumber());

        return repository.save(existing);
    }

    @Transactional
    public Customer patchCustomer(Long id, CustomerPatchRequest patchRequest) {
        Customer existing = getCustomerById(id);

        if (patchRequest.getFirstName() != null) existing.setFirstName(patchRequest.getFirstName());
        if (patchRequest.getLastName() != null) existing.setLastName(patchRequest.getLastName());
        if (patchRequest.getEmail() != null) {
            if (!existing.getEmail().equals(patchRequest.getEmail()) && repository.existsByEmail(patchRequest.getEmail())) {
                throw new DuplicateResourceException("Customer with email " + patchRequest.getEmail() + " already exists");
            }
            existing.setEmail(patchRequest.getEmail());
        }
        if (patchRequest.getPhoneNumber() != null) existing.setPhoneNumber(patchRequest.getPhoneNumber());

        return repository.save(existing);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer existing = getCustomerById(id);
        repository.delete(existing);
    }
}