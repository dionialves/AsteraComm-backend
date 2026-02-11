package com.dionialves.AsteraComm.domain.core.circuit.status;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.Endpoint;

@Component
public class EndpointStatusFactory {

    private final CircuitStatusRepository endpointStatusRepository;

    @Autowired
    public EndpointStatusFactory(CircuitStatusRepository endpointStatusRepository) {
        this.endpointStatusRepository = endpointStatusRepository;
    }

    public Optional<CircuitStatus> getByEndpoint(Endpoint endpoint) {
        return endpointStatusRepository.findTopByEndpointOrderByCheckedAtDesc(endpoint);
    }

    public List<CircuitStatus> getAll() {
        return endpointStatusRepository.findAll();
    }
}
