package com.dionialves.AsteraConnect.controller;

import com.dionialves.AsteraConnect.dto.EndPointDTO;
import com.dionialves.AsteraConnect.repository.EndPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
