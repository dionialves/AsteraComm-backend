package com.dionialves.AsteraComm.did;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.did.dto.DIDCreateDTO;
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
class DIDServiceTest {

    @Mock
    private DIDRepository didRepository;

    @Mock
    private CircuitRepository circuitRepository;

    @Mock
    private AsteriskProvisioningService asteriskProvisioningService;

    @InjectMocks
    private DIDService didService;

    private DID testDID;
    private Circuit testCircuit;

    @BeforeEach
    void setUp() {
        testDID = new DID();
        testDID.setId(1L);
        testDID.setNumber("4933001234");
        testDID.setCircuitNumber(null);

        testCircuit = new Circuit();
        testCircuit.setNumber("100000");
        testCircuit.setTrunkName("provedor1");
    }

    // --- getAll ---

    @Test
    void getAll_shouldDelegateToRepository() {
        Page<DID> page = new PageImpl<>(List.of(testDID));
        when(didRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<DID> result = didService.getAll("", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(didRepository).findAll(PageRequest.of(0, 10));
    }

    // --- findById ---

    @Test
    void findById_shouldReturnDID_whenExists() {
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));

        Optional<DID> result = didService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo("4933001234");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(didRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DID> result = didService.findById(99L);

        assertThat(result).isEmpty();
    }

    // --- create ---

    @Test
    void create_shouldSaveDID_whenNumberIsUniqueAndDigitsOnly() {
        DIDCreateDTO dto = new DIDCreateDTO("4933001234");
        when(didRepository.existsByNumber("4933001234")).thenReturn(false);
        when(didRepository.save(any(DID.class))).thenReturn(testDID);

        DID result = didService.create(dto);

        verify(didRepository).save(argThat(d -> d.getNumber().equals("4933001234")));
        assertThat(result.getNumber()).isEqualTo("4933001234");
    }

    @Test
    void create_shouldThrowBusinessException_whenNumberAlreadyExists() {
        DIDCreateDTO dto = new DIDCreateDTO("4933001234");
        when(didRepository.existsByNumber("4933001234")).thenReturn(true);

        assertThatThrownBy(() -> didService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DID já cadastrado");
    }

    @Test
    void create_shouldThrowBusinessException_whenNumberContainsNonDigits() {
        DIDCreateDTO dto = new DIDCreateDTO("(49) 3300-1234");

        assertThatThrownBy(() -> didService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("apenas dígitos");
    }

    @Test
    void create_shouldThrowBusinessException_whenNumberHasWrongLength() {
        DIDCreateDTO dtoShort = new DIDCreateDTO("123456789");
        DIDCreateDTO dtoLong  = new DIDCreateDTO("12345678901");

        assertThatThrownBy(() -> didService.create(dtoShort))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exatamente 10 dígitos");

        assertThatThrownBy(() -> didService.create(dtoLong))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exatamente 10 dígitos");
    }

    // --- linkToCircuit ---

    @Test
    void linkToCircuit_shouldSetCircuitNumberAndProvisionDid_whenDIDFreeAndCircuitExists() {
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));
        when(circuitRepository.findByNumber("100000")).thenReturn(Optional.of(testCircuit));
        when(didRepository.save(any(DID.class))).thenReturn(testDID);

        didService.linkToCircuit(1L, "100000");

        verify(didRepository).save(argThat(d -> "100000".equals(d.getCircuitNumber())));
        verify(asteriskProvisioningService).provisionDid(any(DID.class), eq(testCircuit));
    }

    @Test
    void linkToCircuit_shouldThrowBusinessException_whenAlreadyLinked() {
        testDID.setCircuitNumber("100000");
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));

        assertThatThrownBy(() -> didService.linkToCircuit(1L, "100001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está vinculado");

        verifyNoInteractions(asteriskProvisioningService);
    }

    @Test
    void linkToCircuit_shouldThrowNotFoundException_whenCircuitNotExists() {
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));
        when(circuitRepository.findByNumber("999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> didService.linkToCircuit(1L, "999999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Circuito não encontrado");

        verifyNoInteractions(asteriskProvisioningService);
    }

    @Test
    void linkToCircuit_shouldThrowNotFoundException_whenDIDNotExists() {
        when(didRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> didService.linkToCircuit(99L, "100000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("DID não encontrado");

        verifyNoInteractions(asteriskProvisioningService);
    }

    // --- unlinkFromCircuit ---

    @Test
    void unlinkFromCircuit_shouldClearCircuitNumberAndDeprovisionDid_whenLinked() {
        testDID.setCircuitNumber("100000");
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));
        when(circuitRepository.findByNumber("100000")).thenReturn(Optional.of(testCircuit));
        when(didRepository.save(any(DID.class))).thenReturn(testDID);

        didService.unlinkFromCircuit(1L);

        verify(didRepository).save(argThat(d -> d.getCircuitNumber() == null));
        verify(asteriskProvisioningService).deprovisionDid("4933001234", "provedor1");
    }

    @Test
    void unlinkFromCircuit_shouldThrowBusinessException_whenNotLinked() {
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));

        assertThatThrownBy(() -> didService.unlinkFromCircuit(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está vinculado");

        verifyNoInteractions(asteriskProvisioningService);
    }

    @Test
    void unlinkFromCircuit_shouldThrowNotFoundException_whenDIDNotExists() {
        when(didRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> didService.unlinkFromCircuit(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("DID não encontrado");

        verifyNoInteractions(asteriskProvisioningService);
    }

    // --- delete ---

    @Test
    void delete_shouldDeleteDID_whenNotLinkedToCircuit() {
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));

        didService.delete(1L);

        verify(didRepository).delete(testDID);
    }

    @Test
    void delete_shouldThrowBusinessException_whenLinkedToCircuit() {
        testDID.setCircuitNumber("100000");
        when(didRepository.findById(1L)).thenReturn(Optional.of(testDID));

        assertThatThrownBy(() -> didService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vinculado a um circuito");
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(didRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> didService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("DID não encontrado");
    }
}
