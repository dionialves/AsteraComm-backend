package com.dionialves.AsteraComm.asterisk.provisioning;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.dialplan.DialplanGeneratorService;
import com.dionialves.AsteraComm.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatusRepository;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.asterisk.registration.PsRegistration;
import com.dionialves.AsteraComm.asterisk.registration.PsRegistrationRepository;
import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.trunk.Trunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsteriskProvisioningServiceTest {

    @Mock
    private AorRepository aorRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private EndpointStatusRepository endpointStatusRepository;

    @Mock
    private PsRegistrationRepository psRegistrationRepository;

    @Mock
    private AmiService amiService;

    @Mock
    private DialplanGeneratorService dialplanGeneratorService;

    @InjectMocks
    private AsteriskProvisioningService asteriskProvisioningService;

    private Circuit testCircuit;
    private Trunk testTrunk;

    @BeforeEach
    void setUp() {
        testCircuit = new Circuit();
        testCircuit.setNumber("1001");
        testCircuit.setPassword("secret");
        testCircuit.setTrunkName("provedor1");

        testTrunk = new Trunk();
        testTrunk.setName("provedor1");
        testTrunk.setHost("sip.provedor1.com.br");
        testTrunk.setUsername("user123");
        testTrunk.setPassword("senha123");
    }

    @Test
    void provision_shouldCreateAors() {
        asteriskProvisioningService.provision(testCircuit);

        verify(aorRepository).save(argThat(a -> a.getId().equals("1001")));
    }

    @Test
    void provision_shouldCreateAuth() {
        asteriskProvisioningService.provision(testCircuit);

        verify(authRepository).save(argThat(a ->
                a.getId().equals("1001") && a.getPassword().equals("secret")));
    }

    @Test
    void provision_shouldCreateEndpoint() {
        asteriskProvisioningService.provision(testCircuit);

        verify(endpointRepository).save(argThat(e -> e.getId().equals("1001")));
    }

    @Test
    void provision_shouldCreateTwoExtensions() {
        asteriskProvisioningService.provision(testCircuit);

        verify(extensionRepository, times(2)).save(any());
    }

    @Test
    void provision_shouldCallPjsipReload() {
        asteriskProvisioningService.provision(testCircuit);

        verify(amiService).sendCommand("pjsip reload");
    }

    @Test
    void provision_shouldSetCircuitEndpointContextToInternalTrunk() {
        asteriskProvisioningService.provision(testCircuit);

        verify(endpointRepository).save(argThat(e ->
                e.getId().equals("1001")
                && e.getContext().equals("internal-provedor1")));
    }

    @Test
    void reprovision_shouldUpdateAuthPasswordAndCallPjsipReload() {
        Auth auth = new Auth();
        auth.setId("1001");
        auth.setPassword("oldpassword");
        when(authRepository.findById("1001")).thenReturn(Optional.of(auth));

        asteriskProvisioningService.reprovision(testCircuit, "provedor1");

        verify(authRepository).save(argThat(a -> a.getPassword().equals("secret")));
        verify(amiService).sendCommand("pjsip reload");
    }

    @Test
    void deprovision_shouldRemoveAllAsteriskEntities() {
        Aors aors = new Aors();
        aors.setId("1001");
        Auth auth = new Auth();
        auth.setId("1001");
        Endpoint endpoint = new Endpoint();
        endpoint.setId("1001");
        endpoint.setAors(aors);
        endpoint.setAuth(auth);
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(endpoint));

        asteriskProvisioningService.deprovision(testCircuit);

        verify(extensionRepository).deleteByExten("1001");
        verify(endpointStatusRepository).deleteByEndpoint(endpoint);
        verify(endpointRepository).delete(endpoint);
        verify(authRepository).delete(auth);
        verify(aorRepository).delete(aors);
    }

    @Test
    void deprovision_shouldCallPjsipAndDialplanReload() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId("1001");
        endpoint.setAors(new Aors());
        endpoint.setAuth(new Auth());
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(endpoint));

        asteriskProvisioningService.deprovision(testCircuit);

        verify(amiService).sendCommand("pjsip reload");
        verify(amiService).sendCommand("dialplan reload");
    }

    @Test
    void deprovision_shouldDoNothing_whenEndpointNotFound() {
        when(endpointRepository.findById("1001")).thenReturn(Optional.empty());

        asteriskProvisioningService.deprovision(testCircuit);

        verify(extensionRepository).deleteByExten("1001");
        verifyNoInteractions(aorRepository);
        verifyNoInteractions(authRepository);
        verifyNoMoreInteractions(endpointRepository);
    }

    // === Trunk provisioning ===

    @Test
    void provisionTrunk_shouldCreateAorsWithStaticContact() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(aorRepository).save(argThat(a ->
                a.getId().equals("provedor1")
                && a.getContact().equals("sip:sip.provedor1.com.br")));
    }

    @Test
    void provisionTrunk_shouldCreateAuth() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(authRepository).save(argThat(a ->
                a.getId().equals("provedor1")
                && a.getUsername().equals("user123")
                && a.getPassword().equals("senha123")));
    }

    @Test
    void provisionTrunk_shouldCreateEndpointWithOutboundAuth() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(endpointRepository).save(argThat(e ->
                e.getId().equals("provedor1")
                && e.getOutboundAuth().equals("provedor1")
                && e.getAuth() == null));
    }

    @Test
    void provisionTrunk_shouldCreateRegistration() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(psRegistrationRepository).save(argThat(r ->
                r.getId().equals("provedor1")
                && r.getServerUri().equals("sip:sip.provedor1.com.br")
                && r.getClientUri().equals("sip:user123@sip.provedor1.com.br")
                && r.getOutboundAuth().equals("provedor1")));
    }

    @Test
    void provisionTrunk_shouldCallPjsipReload() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(amiService).sendCommand("pjsip reload");
    }

    @Test
    void reprovisionTrunk_shouldUpdateAuthPasswordAndRegistrationAndCallPjsipReload() {
        Auth auth = new Auth();
        auth.setId("provedor1");
        auth.setPassword("senhaantiga");

        PsRegistration reg = new PsRegistration();
        reg.setId("provedor1");
        reg.setClientUri("sip:userold@sip.provedor1.com.br");

        when(authRepository.findById("provedor1")).thenReturn(Optional.of(auth));
        when(psRegistrationRepository.findById("provedor1")).thenReturn(Optional.of(reg));

        asteriskProvisioningService.reprovisionTrunk(testTrunk);

        verify(authRepository).save(argThat(a -> a.getPassword().equals("senha123")));
        verify(psRegistrationRepository).save(argThat(r ->
                r.getClientUri().equals("sip:user123@sip.provedor1.com.br")
                && r.getServerUri().equals("sip:sip.provedor1.com.br")));
        verify(amiService).sendCommand("pjsip reload");
    }

    @Test
    void deprovisionTrunk_shouldRemoveAllAsteriskEntities() {
        PsRegistration reg = new PsRegistration();
        reg.setId("provedor1");

        when(psRegistrationRepository.findById("provedor1")).thenReturn(Optional.of(reg));
        when(endpointRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(authRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(aorRepository.findById("provedor1")).thenReturn(Optional.empty());

        asteriskProvisioningService.deprovisionTrunk(testTrunk);

        verify(psRegistrationRepository).delete(reg);
    }

    @Test
    void deprovisionTrunk_shouldCallPjsipReload() {
        when(psRegistrationRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(endpointRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(authRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(aorRepository.findById("provedor1")).thenReturn(Optional.empty());

        asteriskProvisioningService.deprovisionTrunk(testTrunk);

        verify(amiService).sendCommand("pjsip reload");
    }

    // === DialplanGeneratorService integration ===

    @Test
    void provisionTrunk_shouldCallGenerateAndReload() {
        asteriskProvisioningService.provisionTrunk(testTrunk);

        verify(dialplanGeneratorService).generateAndReload();
    }

    @Test
    void deprovisionTrunk_shouldCallGenerateAndReload() {
        when(psRegistrationRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(endpointRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(authRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(aorRepository.findById("provedor1")).thenReturn(Optional.empty());

        asteriskProvisioningService.deprovisionTrunk(testTrunk);

        verify(dialplanGeneratorService).generateAndReload();
    }

    @Test
    void reprovisionTrunk_shouldNotCallGenerateAndReload() {
        when(authRepository.findById("provedor1")).thenReturn(Optional.empty());
        when(psRegistrationRepository.findById("provedor1")).thenReturn(Optional.empty());

        asteriskProvisioningService.reprovisionTrunk(testTrunk);

        verify(dialplanGeneratorService, never()).generateAndReload();
    }
}
