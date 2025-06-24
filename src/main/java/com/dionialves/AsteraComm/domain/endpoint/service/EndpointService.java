package com.dionialves.AsteraComm.domain.endpoint.service;

import java.util.Optional;

import com.dionialves.AsteraComm.domain.endpoint.entity.Endpoint;
import com.dionialves.AsteraComm.domain.endpoint.projection.EndpointProjection;
import com.dionialves.AsteraComm.domain.endpoint.repository.EndpointRepository;
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
