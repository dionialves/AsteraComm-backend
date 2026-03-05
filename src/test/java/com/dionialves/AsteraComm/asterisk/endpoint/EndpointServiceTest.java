package com.dionialves.AsteraComm.asterisk.endpoint;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.dto.EndpointCreateDTO;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
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
class EndpointServiceTest {

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private AorRepository aorRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private EndpointStatusRepository endpointStatusRepository;

    @InjectMocks
    private EndpointService endpointService;

    private Endpoint testEndpoint;

    @BeforeEach
    void setUp() {
        Aors aors = new Aors();
        aors.setId("1001");

        Auth auth = new Auth();
        auth.setId("1001");
        auth.setPassword("secret");

        testEndpoint = new Endpoint();
        testEndpoint.setId("1001");
        testEndpoint.setAors(aors);
        testEndpoint.setAuth(auth);
    }

    @Test
    void getAllEndpointData_shouldReturnProjection() {
        Page<EndpointProjection> page = new PageImpl<>(List.of());
        when(endpointRepository.findAllEndpoint(anyString(), any(Pageable.class))).thenReturn(page);

        Page<EndpointProjection> result = endpointService.getAllEndpointData("", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(endpointRepository).findAllEndpoint("", PageRequest.of(0, 10));
    }

    @Test
    void findById_shouldReturnEndpoint_whenExists() {
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(testEndpoint));

        Optional<Endpoint> result = endpointService.findByid("1001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("1001");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(endpointRepository.findById("9999")).thenReturn(Optional.empty());

        Optional<Endpoint> result = endpointService.findByid("9999");

        assertThat(result).isEmpty();
    }

    @Test
    void create_shouldPersistEndpoint_AorsAndAuth() {
        EndpointCreateDTO dto = new EndpointCreateDTO("1002", "mypassword");
        when(endpointRepository.existsById("1002")).thenReturn(false);
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(testEndpoint);

        endpointService.create(dto);

        verify(aorRepository).save(argThat(a -> a.getId().equals("1002")));
        verify(authRepository).save(argThat(a -> a.getId().equals("1002") && a.getPassword().equals("mypassword")));
        verify(endpointRepository).save(argThat(e -> e.getId().equals("1002")));
    }

    @Test
    void create_shouldThrowBusinessException_whenIdAlreadyExists() {
        EndpointCreateDTO dto = new EndpointCreateDTO("1001", "password");
        when(endpointRepository.existsById("1001")).thenReturn(true);

        assertThatThrownBy(() -> endpointService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Circuito já existe");
    }

    @Test
    void update_shouldUpdatePasswordOnRelatedEntities() {
        EndpointCreateDTO dto = new EndpointCreateDTO("1001", "newpassword");
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(testEndpoint));

        endpointService.update("1001", dto);

        verify(authRepository).save(argThat(a -> a.getPassword().equals("newpassword")));
    }

    @Test
    void update_shouldThrowNotFoundException_whenNotExists() {
        EndpointCreateDTO dto = new EndpointCreateDTO("9999", "password");
        when(endpointRepository.findById("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> endpointService.update("9999", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Circuito não encontrado");
    }

    @Test
    void delete_shouldRemoveEndpointAndRelatedEntities() {
        when(endpointRepository.findById("1001")).thenReturn(Optional.of(testEndpoint));

        endpointService.delete("1001");

        verify(extensionRepository).deleteByExten(testEndpoint);
        verify(endpointStatusRepository).deleteByEndpoint(testEndpoint);
        verify(endpointRepository).delete(testEndpoint);
        verify(authRepository).delete(testEndpoint.getAuth());
        verify(aorRepository).delete(testEndpoint.getAors());
    }

    @Test
    void delete_shouldThrowNotFoundException_whenNotExists() {
        when(endpointRepository.findById("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> endpointService.delete("9999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Circuito não encontrado");
    }
}
