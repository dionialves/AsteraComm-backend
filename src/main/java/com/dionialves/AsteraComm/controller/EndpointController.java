package com.dionialves.AsteraComm.controller;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.service.EndpointService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/circuits")
public class EndpointController {

    @Autowired
    private EndpointService endpointService;

    @GetMapping
    public Page<EndpointDTO> getEndpoints(Pageable pageable) {
        return endpointService.getAllEndpointData(pageable);
    }

}
