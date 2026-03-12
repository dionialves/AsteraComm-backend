package com.dionialves.AsteraComm.customer;

import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.customer.dto.CustomerCreateDTO;
import com.dionialves.AsteraComm.exception.BusinessException;
import com.dionialves.AsteraComm.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CircuitRepository circuitRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Acme Corp");
        testCustomer.setEnabled(true);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());
    }

    // --- getAll ---

    @Test
    void getAll_shouldReturnPagedCustomers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(testCustomer), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<Customer> result = customerService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Acme Corp");
    }

    // --- findById ---

    @Test
    void findById_shouldReturnCustomer_whenExists() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Optional<Customer> result = customerService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Acme Corp");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findById(99L);

        assertThat(result).isEmpty();
    }

    // --- create ---

    @Test
    void create_shouldPersistCustomerWithEnabledTrueAndTimestamps() {
        CustomerCreateDTO dto = new CustomerCreateDTO("Acme Corp", null);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = customerService.create(dto);

        verify(customerRepository).save(argThat(c ->
                c.getName().equals("Acme Corp")
                && c.isEnabled()
                && c.getCreatedAt() != null
                && c.getUpdatedAt() != null));
        assertThat(result).isNotNull();
    }

    @Test
    void create_shouldRespectEnabledFlag_whenExplicitlyFalse() {
        CustomerCreateDTO dto = new CustomerCreateDTO("Inactive Corp", false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        customerService.create(dto);

        verify(customerRepository).save(argThat(c -> !c.isEnabled()));
    }

    // --- update ---

    @Test
    void update_shouldUpdateNameAndRefreshUpdatedAt() {
        CustomerCreateDTO dto = new CustomerCreateDTO("New Name", true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        customerService.update(1L, dto);

        verify(customerRepository).save(argThat(c ->
                c.getName().equals("New Name")
                && c.getUpdatedAt() != null));
    }

    @Test
    void update_shouldThrowNotFoundException_whenNotExists() {
        CustomerCreateDTO dto = new CustomerCreateDTO("Name", true);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(99L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    // --- delete ---

    @Test
    void delete_shouldRemoveCustomer_whenNoCircuitsLinked() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(circuitRepository.existsByCustomerId(1L)).thenReturn(false);

        customerService.delete(1L);

        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void delete_shouldThrowBusinessException_whenCustomerHasCircuits() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(circuitRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("circuito");

        verify(customerRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    // --- disable ---

    @Test
    void disable_shouldSetEnabledFalseAndUpdateTimestamp() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        customerService.disable(1L);

        verify(customerRepository).save(argThat(c ->
                !c.isEnabled() && c.getUpdatedAt() != null));
    }

    @Test
    void disable_shouldWork_evenWhenCustomerHasCircuits() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        customerService.disable(1L);

        verify(customerRepository).save(any(Customer.class));
        verifyNoInteractions(circuitRepository);
    }

    @Test
    void disable_shouldThrowNotFoundException_whenNotExists() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.disable(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }
}
