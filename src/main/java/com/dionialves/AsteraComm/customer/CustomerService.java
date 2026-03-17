package com.dionialves.AsteraComm.customer;

import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.customer.dto.CustomerCreateDTO;
import com.dionialves.AsteraComm.exception.BusinessException;
import com.dionialves.AsteraComm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CircuitRepository circuitRepository;

    public Page<Customer> getAll(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer create(CustomerCreateDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.name());
        customer.setEnabled(dto.enabled() == null || dto.enabled());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerCreateDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        customer.setName(dto.name());
        customer.setEnabled(dto.enabled() == null || dto.enabled());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        if (circuitRepository.existsByCustomerId(id)) {
            throw new BusinessException("Cliente possui circuito(s) vinculado(s). Use a desativação.");
        }
        customerRepository.delete(customer);
    }

    public Customer disable(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        customer.setEnabled(false);
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }
}
