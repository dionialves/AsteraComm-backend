package com.dionialves.AsteraComm.domain.endpoint.factory;

import java.util.List;
import java.util.Optional;

import com.dionialves.AsteraComm.domain.endpoint.entity.EndpointStatus;
import com.dionialves.AsteraComm.domain.endpoint.repository.EndpointStatusRepository;
import com.dionialves.AsteraComm.domain.endpoint.entity.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
