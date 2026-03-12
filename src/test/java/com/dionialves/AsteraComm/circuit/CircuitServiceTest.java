package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
import com.dionialves.AsteraComm.customer.Customer;
import com.dionialves.AsteraComm.customer.CustomerRepository;
import com.dionialves.AsteraComm.did.DIDRepository;
import com.dionialves.AsteraComm.exception.BusinessException;
import com.dionialves.AsteraComm.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircuitServiceTest {

    @Mock
    private CircuitRepository circuitRepository;

    @Mock
    private DIDRepository didRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AsteriskProvisioningService asteriskProvisioningService;

    @InjectMocks
    private CircuitService circuitService;

    private Circuit testCircuit;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Acme Corp");
        testCustomer.setEnabled(true);

        testCircuit = new Circuit();
        testCircuit.setNumber("100000");
        testCircuit.setPassword("secret");
        testCircuit.setTrunkName("opasuite");
        testCircuit.setCustomer(testCustomer);
    }

    @Test
    void getAll_shouldDelegateToRepository() {
        Page<CircuitProjection> page = new PageImpl<>(List.of());
        when(circuitRepository.findAllCircuits(anyString(), any(Pageable.class))).thenReturn(page);

        Page<CircuitProjection> result = circuitService.getAll("", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(circuitRepository).findAllCircuits("", PageRequest.of(0, 10));
    }

    @Test
    void findByNumber_shouldReturnCircuit_whenExists() {
        when(circuitRepository.findById("100000")).thenReturn(Optional.of(testCircuit));

        Optional<Circuit> result = circuitService.findByNumber("100000");

        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo("100000");
    }

    @Test
    void findByNumber_shouldReturnEmpty_whenNotExists() {
        when(circuitRepository.findById("999999")).thenReturn(Optional.empty());

        Optional<Circuit> result = circuitService.findByNumber("999999");

        assertThat(result).isEmpty();
    }

    @Test
    void create_shouldGenerateCodeStartingAt100000_whenNoCircuitsExist() {
        CircuitCreateDTO dto = new CircuitCreateDTO("mypassword", "opasuite", 1L);
        when(circuitRepository.findMaxCode()).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(circuitRepository.save(any(Circuit.class))).thenReturn(testCircuit);

        circuitService.create(dto);

        verify(circuitRepository).save(argThat(c ->
                c.getNumber().equals("100000")
                && c.getPassword().equals("mypassword")
                && c.getTrunkName().equals("opasuite")
                && c.getCustomer().getId().equals(1L)));
        verify(asteriskProvisioningService).provision(any(Circuit.class));
    }

    @Test
    void create_shouldIncrementCode_whenCircuitsExist() {
        CircuitCreateDTO dto = new CircuitCreateDTO("mypassword", "opasuite", 1L);
        when(circuitRepository.findMaxCode()).thenReturn(Optional.of("100004"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(circuitRepository.save(any(Circuit.class))).thenReturn(testCircuit);

        circuitService.create(dto);

        verify(circuitRepository).save(argThat(c -> c.getNumber().equals("100005")));
    }

    @Test
    void create_shouldThrowNotFoundException_whenCustomerNotExists() {
        CircuitCreateDTO dto = new CircuitCreateDTO("mypassword", "opasuite", 99L);
        when(circuitRepository.findMaxCode()).thenReturn(Optional.empty());
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.create(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");

        verify(circuitRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdatePasswordAndCallReprovision() {
        CircuitCreateDTO dto = new CircuitCreateDTO("newpassword", "opasuite", 1L);
        when(circuitRepository.findById("100000")).thenReturn(Optional.of(testCircuit));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(circuitRepository.save(any(Circuit.class))).thenReturn(testCircuit);

        circuitService.update("100000", dto);

        verify(circuitRepository).save(argThat(c -> c.getPassword().equals("newpassword")));
        verify(asteriskProvisioningService).reprovision(any(Circuit.class), eq("opasuite"));
    }

    @Test
    void update_shouldThrowNotFoundException_whenCircuitNotExists() {
        CircuitCreateDTO dto = new CircuitCreateDTO("password", "opasuite", 1L);
        when(circuitRepository.findById("999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.update("999999", dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Circuito não encontrado");
    }

    @Test
    void update_shouldThrowNotFoundException_whenCustomerNotExists() {
        CircuitCreateDTO dto = new CircuitCreateDTO("password", "opasuite", 99L);
        when(circuitRepository.findById("100000")).thenReturn(Optional.of(testCircuit));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.update("100000", dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    void delete_shouldCallDeprovisionAndDeleteCircuit() {
        when(circuitRepository.findById("100000")).thenReturn(Optional.of(testCircuit));
        when(didRepository.existsByCircuitNumber("100000")).thenReturn(false);

        circuitService.delete("100000");

        verify(asteriskProvisioningService).deprovision(testCircuit);
        verify(circuitRepository).delete(testCircuit);
    }

    @Test
    void delete_shouldThrowBusinessException_whenCircuitHasLinkedDids() {
        when(circuitRepository.findById("100000")).thenReturn(Optional.of(testCircuit));
        when(didRepository.existsByCircuitNumber("100000")).thenReturn(true);

        assertThatThrownBy(() -> circuitService.delete("100000"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DID");

        verifyNoInteractions(asteriskProvisioningService);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(circuitRepository.findById("999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.delete("999999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Circuito não encontrado");
    }
}
