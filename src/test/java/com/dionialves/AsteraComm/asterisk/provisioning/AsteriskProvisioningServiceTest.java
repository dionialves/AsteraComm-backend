package com.dionialves.AsteraComm.asterisk.provisioning;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatusRepository;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.circuit.Circuit;
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
    private AmiService amiService;

    @InjectMocks
    private AsteriskProvisioningService asteriskProvisioningService;

    private Circuit testCircuit;

    @BeforeEach
    void setUp() {
        testCircuit = new Circuit();
        testCircuit.setNumber("1001");
        testCircuit.setPassword("secret");
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
    void reprovision_shouldUpdateAuthPasswordAndCallPjsipReload() {
        Auth auth = new Auth();
        auth.setId("1001");
        auth.setPassword("oldpassword");
        when(authRepository.findById("1001")).thenReturn(Optional.of(auth));

        asteriskProvisioningService.reprovision(testCircuit);

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

        verify(extensionRepository).deleteByExten(endpoint);
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

        verifyNoInteractions(extensionRepository);
        verifyNoInteractions(aorRepository);
        verifyNoInteractions(authRepository);
        verifyNoMoreInteractions(endpointRepository);
    }
}
