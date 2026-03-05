package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
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
    private AsteriskProvisioningService asteriskProvisioningService;

    @InjectMocks
    private CircuitService circuitService;

    private Circuit testCircuit;

    @BeforeEach
    void setUp() {
        testCircuit = new Circuit();
        testCircuit.setNumber("1001");
        testCircuit.setPassword("secret");
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
        when(circuitRepository.findById("1001")).thenReturn(Optional.of(testCircuit));

        Optional<Circuit> result = circuitService.findByNumber("1001");

        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo("1001");
    }

    @Test
    void findByNumber_shouldReturnEmpty_whenNotExists() {
        when(circuitRepository.findById("9999")).thenReturn(Optional.empty());

        Optional<Circuit> result = circuitService.findByNumber("9999");

        assertThat(result).isEmpty();
    }

    @Test
    void create_shouldSaveCircuitAndCallProvision() {
        CircuitCreateDTO dto = new CircuitCreateDTO("1002", "mypassword");
        when(circuitRepository.existsById("1002")).thenReturn(false);
        when(circuitRepository.save(any(Circuit.class))).thenReturn(testCircuit);

        circuitService.create(dto);

        verify(circuitRepository).save(argThat(c ->
                c.getNumber().equals("1002") && c.getPassword().equals("mypassword")));
        verify(asteriskProvisioningService).provision(any(Circuit.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenNumberAlreadyExists() {
        CircuitCreateDTO dto = new CircuitCreateDTO("1001", "password");
        when(circuitRepository.existsById("1001")).thenReturn(true);

        assertThatThrownBy(() -> circuitService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Circuito já existe");
    }

    @Test
    void update_shouldUpdatePasswordAndCallReprovision() {
        CircuitCreateDTO dto = new CircuitCreateDTO("1001", "newpassword");
        when(circuitRepository.findById("1001")).thenReturn(Optional.of(testCircuit));
        when(circuitRepository.save(any(Circuit.class))).thenReturn(testCircuit);

        circuitService.update("1001", dto);

        verify(circuitRepository).save(argThat(c -> c.getPassword().equals("newpassword")));
        verify(asteriskProvisioningService).reprovision(any(Circuit.class));
    }

    @Test
    void update_shouldThrowNotFoundException_whenNotExists() {
        CircuitCreateDTO dto = new CircuitCreateDTO("9999", "password");
        when(circuitRepository.findById("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.update("9999", dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Circuito não encontrado");
    }

    @Test
    void delete_shouldCallDeprovisionAndDeleteCircuit() {
        when(circuitRepository.findById("1001")).thenReturn(Optional.of(testCircuit));

        circuitService.delete("1001");

        verify(asteriskProvisioningService).deprovision(testCircuit);
        verify(circuitRepository).delete(testCircuit);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(circuitRepository.findById("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> circuitService.delete("9999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Circuito não encontrado");
    }
}
