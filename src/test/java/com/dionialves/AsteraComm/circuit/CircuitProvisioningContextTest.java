package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatusRepository;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.asterisk.dialplan.DialplanGeneratorService;
import com.dionialves.AsteraComm.asterisk.provisioning.AmiService;
import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.asterisk.registration.PsRegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Testa o provisionamento de circuitos conforme US-009:
 * - ps_endpoints.context = "internal-<tronco>"
 * - Extensions de entrada criadas em "pstn-<tronco>"
 * - Troca de tronco no reprovision move as extensions
 */
@ExtendWith(MockitoExtension.class)
class CircuitProvisioningContextTest {

    @Mock private AorRepository aorRepository;
    @Mock private AuthRepository authRepository;
    @Mock private EndpointRepository endpointRepository;
    @Mock private ExtensionRepository extensionRepository;
    @Mock private EndpointStatusRepository endpointStatusRepository;
    @Mock private PsRegistrationRepository psRegistrationRepository;
    @Mock private AmiService amiService;
    @Mock private DialplanGeneratorService dialplanGeneratorService;

    @InjectMocks
    private AsteriskProvisioningService provisioningService;

    private Circuit circuit;

    @BeforeEach
    void setUp() {
        circuit = new Circuit();
        circuit.setNumber("1001");
        circuit.setPassword("secret");
        circuit.setTrunkName("opasuite");
    }

    // === Contexto do ps_endpoint do circuito ===

    @Test
    void provision_shouldSetEndpointContextToInternalTrunk() {
        provisioningService.provision(circuit);

        verify(endpointRepository).save(argThat(e ->
                e.getId().equals("1001")
                && e.getContext().equals("internal-opasuite")));
    }

    // === Extensions de entrada em pstn-<tronco> ===

    @Test
    void provision_shouldCreateInboundDialExtensionInPstnContext() {
        provisioningService.provision(circuit);

        verify(extensionRepository).save(argThat(e ->
                e.getContext().equals("pstn-opasuite")
                && e.getExten().equals("1001")
                && e.getPriority() == 1
                && e.getApp().equals("Dial")
                && e.getAppdata().equals("PJSIP/1001,60")));
    }

    @Test
    void provision_shouldCreateInboundHangupExtensionInPstnContext() {
        provisioningService.provision(circuit);

        verify(extensionRepository).save(argThat(e ->
                e.getContext().equals("pstn-opasuite")
                && e.getExten().equals("1001")
                && e.getPriority() == 2
                && e.getApp().equals("Hangup")));
    }

    // === Reprovisionamento — troca de tronco ===

    @Test
    void reprovision_shouldDeleteOldPstnExtensionsWhenTrunkChanges() {
        Circuit updated = new Circuit();
        updated.setNumber("1001");
        updated.setPassword("secret");
        updated.setTrunkName("tellcheap");

        when(authRepository.findById("1001")).thenReturn(Optional.of(new Auth()));
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(new Endpoint()));

        provisioningService.reprovision(updated, "opasuite");

        verify(extensionRepository).deleteByExtenAndContext("1001", "pstn-opasuite");
    }

    @Test
    void reprovision_shouldUpdateEndpointContextWhenTrunkChanges() {
        Circuit updated = new Circuit();
        updated.setNumber("1001");
        updated.setPassword("secret");
        updated.setTrunkName("tellcheap");

        Endpoint endpoint = new Endpoint();
        endpoint.setId("1001");
        when(authRepository.findById("1001")).thenReturn(Optional.of(new Auth()));
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(endpoint));

        provisioningService.reprovision(updated, "opasuite");

        verify(endpointRepository).save(argThat(e ->
                e.getId().equals("1001")
                && e.getContext().equals("internal-tellcheap")));
    }

    @Test
    void reprovision_shouldCreateNewPstnExtensionsForNewTrunk() {
        Circuit updated = new Circuit();
        updated.setNumber("1001");
        updated.setPassword("secret");
        updated.setTrunkName("tellcheap");

        when(authRepository.findById("1001")).thenReturn(Optional.of(new Auth()));
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(new Endpoint()));

        provisioningService.reprovision(updated, "opasuite");

        verify(extensionRepository).save(argThat(e ->
                e.getContext().equals("pstn-tellcheap")
                && e.getExten().equals("1001")
                && e.getPriority() == 1
                && e.getApp().equals("Dial")
                && e.getAppdata().equals("PJSIP/1001,60")));
    }

    @Test
    void reprovision_shouldNotMoveExtensionsWhenTrunkUnchanged() {
        when(authRepository.findById("1001")).thenReturn(Optional.of(new Auth()));

        provisioningService.reprovision(circuit, "opasuite");

        verify(extensionRepository, never()).deleteByExtenAndContext(any(), any());
        verifyNoInteractions(endpointRepository);
    }

    // === Deprovisionamento ===

    @Test
    void deprovision_shouldDeleteAllInboundExtensionsViaBulkDelete() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId("1001");
        endpoint.setAors(new Aors());
        endpoint.setAuth(new Auth());
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(endpoint));

        provisioningService.deprovision(circuit);

        verify(extensionRepository).deleteByExten("1001");
    }
}
