package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.factory.EndpointFactory;
import com.dionialves.AsteraComm.factory.EndpointStatusFactory;
import com.dionialves.AsteraComm.repository.EndpointRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndpointService {

    @Autowired
    private EndpointRepository endpointRepository;

    public Page<EndpointDTO> getAllEndpointData(Pageable pageable) {
        List<EndpointDTO> result = endpointRepository.getRepositoryDTO();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());
        List<EndpointDTO> pagedList = result.subList(start, end);

        return new PageImpl<>(pagedList, pageable, result.size());
    }
}
