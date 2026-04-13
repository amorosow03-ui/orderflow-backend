package com.alexander.orderflow.customer.repository;

import com.alexander.orderflow.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}