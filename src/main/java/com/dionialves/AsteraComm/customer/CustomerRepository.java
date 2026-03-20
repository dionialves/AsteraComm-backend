package com.dionialves.AsteraComm.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByNameContainingIgnoreCase(String search, Pageable pageable);

    Page<Customer> findByEnabled(boolean enabled, Pageable pageable);

    Page<Customer> findByEnabledAndNameContainingIgnoreCase(boolean enabled, String search, Pageable pageable);
}
