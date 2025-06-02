package com.dionialves.AsteraComm.factory;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.repository.EndpointStatusRepository;

@Component
public class EndpointStatusFactory {

    private final EndpointStatusRepository endpointStatusRepository;

    @Autowired
    public EndpointStatusFactory(EndpointStatusRepository endpointStatusRepository) {
        this.endpointStatusRepository = endpointStatusRepository;
    }

    public Optional<EndpointStatus> getByEndpoint(Endpoint endpoint) {
        return endpointStatusRepository.findTopByEndpointOrderByCheckedAtDesc(endpoint);
    }

    public List<EndpointStatus> getAll() {
        return endpointStatusRepository.findAll();
    }
}
