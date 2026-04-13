package com.alexander.orderflow.order.repository;

import com.alexander.orderflow.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerId(Long customerId);
}