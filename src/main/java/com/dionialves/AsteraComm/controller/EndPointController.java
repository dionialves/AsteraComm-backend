package com.dionialves.AsteraComm.controller;

import com.dionialves.AsteraComm.dto.EndPointDTO;
import com.dionialves.AsteraComm.repository.EndPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/circuits")
public class EndPointController {

    @Autowired
    private EndPointRepository endPointRepository;

    @GetMapping
    public List<EndPointDTO> getAllCircuits() {
        return endPointRepository.getRepositoryDTO();
    }
}
