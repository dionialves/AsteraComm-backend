package com.dionialves.AsteraComm.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.repository.EndpointRepository;

@Component
public class EndpointFactory {

    private final EndpointRepository endpointRepository;

    @Autowired
    public EndpointFactory(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    public Endpoint getById(String id) {
        return endpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endpoint not found: " + id));
    }

    public List<Endpoint> getAll() {
        return endpointRepository.findAll();
    }
}
