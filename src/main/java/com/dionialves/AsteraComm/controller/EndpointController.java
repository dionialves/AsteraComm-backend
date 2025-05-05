package com.dionialves.AsteraComm.controller;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.repository.EndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/circuits")
public class EndpointController {

    @Autowired
    private EndpointRepository endPointRepository;

    @GetMapping
    public List<EndpointDTO> getAllCircuits() {
        return endPointRepository.getRepositoryDTO();
    }
}
