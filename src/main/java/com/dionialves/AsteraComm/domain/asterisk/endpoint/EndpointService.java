package com.dionialves.AsteraComm.domain.asterisk.endpoint;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.AsteraComm.domain.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.domain.asterisk.aors.Aors;
import com.dionialves.AsteraComm.domain.asterisk.auth.Auth;
import com.dionialves.AsteraComm.domain.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.domain.asterisk.endpoint.dto.EndpointCreateDTO;
import com.dionialves.AsteraComm.domain.asterisk.extension.Extension;
import com.dionialves.AsteraComm.domain.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.domain.core.circuit.status.CircuitStatusRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final AorRepository aorRepository;
    private final AuthRepository authRepository;
    private final ExtensionRepository extensionRepository;
    private final CircuitStatusRepository endpointStatusRepository;

    public Page<EndpointProjection> getAllEndpointData(String search, Pageable pageable) {
        return endpointRepository.findAllEndpoint(search, pageable);
    }

    public Optional<Endpoint> findByid(String id) {
        return endpointRepository.findById(id);
    }

    @Transactional
    public Endpoint create(EndpointCreateDTO dto) {
        if (endpointRepository.existsById(dto.number())) {
            throw new RuntimeException("Circuito já existe com este número");
        }

        String number = dto.number();

        // 1. Criar Aors
        Aors aors = new Aors();
        aors.setId(number);
        aors.setDefaultExpiration("60");
        aors.setMaxContacts("1");
        aors.setRemoveExisting("yes");
        aors.setQualifyFrequency("30");
        aorRepository.save(aors);

        // 2. Criar Auth
        Auth auth = new Auth();
        auth.setId(number);
        auth.setAuthType("userpass");
        auth.setUsername(number);
        auth.setPassword(dto.password());
        authRepository.save(auth);

        // 3. Criar Endpoint
        Endpoint endpoint = new Endpoint();
        endpoint.setId(number);
        endpoint.setAors(aors);
        endpoint.setAuth(auth);
        endpoint.setContext("from-internal");
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDirect_media("no");
        endpoint.setForce_rport("yes");
        endpoint.setRewriteContact("yes");
        endpoint.setRtpSymmetric("yes");
        endpoint.setCallerid(number);
        endpointRepository.save(endpoint);

        // 4. Criar Extensions
        Extension extension1 = new Extension();
        extension1.setContext("from-pstn");
        extension1.setExten(endpoint.getId());
        extension1.setPriority(1);
        extension1.setApp("Dial");
        extension1.setAppdata("PJSIP/" + number + ",60");
        extensionRepository.save(extension1);

        Extension extension2 = new Extension();
        extension2.setContext("from-pstn");
        extension2.setExten(endpoint.getId());
        extension2.setPriority(2);
        extension2.setApp("Hangup");
        extensionRepository.save(extension2);

        return endpoint;
    }

    @Transactional
    public Endpoint update(String id, EndpointCreateDTO dto) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Circuito não encontrado"));

        Auth auth = endpoint.getAuth();
        auth.setPassword(dto.password());
        authRepository.save(auth);

        return endpoint;
    }

    @Transactional
    public void delete(String id) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Circuito não encontrado"));

        extensionRepository.deleteByExten(endpoint);
        endpointStatusRepository.deleteByEndpoint(endpoint);
        endpointRepository.delete(endpoint);
        authRepository.delete(endpoint.getAuth());
        aorRepository.delete(endpoint.getAors());
    }
}
