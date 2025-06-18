package com.dionialves.AsteraComm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.projection.EndpointProjection;
import com.dionialves.AsteraComm.service.EndpointService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/circuits")
public class EndpointController {

    @Autowired
    private EndpointService endpointService;

    @GetMapping
    public Page<EndpointProjection> listEndpoints(
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return endpointService.getAllEndpointData(search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endpoint> getEndpoint(@PathVariable String id) {
        return endpointService.findByid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
