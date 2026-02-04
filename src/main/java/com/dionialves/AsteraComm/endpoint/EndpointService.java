package com.dionialves.AsteraComm.endpoint;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    @Autowired
    private EndpointRepository endpointRepository;

    public Page<EndpointProjection> getAllEndpointData(String search, Pageable pageable) {
        return endpointRepository.findAllEndpoint(search, pageable);
    }

    public Optional<Endpoint> findByid(String id) {
        return endpointRepository.findById(id);
    }
}
