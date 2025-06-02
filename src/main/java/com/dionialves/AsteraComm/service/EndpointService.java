package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.factory.EndpointFactory;
import com.dionialves.AsteraComm.factory.EndpointStatusFactory;
import com.dionialves.AsteraComm.repository.EndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EndpointService {

    @Autowired
    private EndpointRepository endpointRepository;

    @Autowired
    private EndpointFactory endpointFactory;

    @Autowired
    private EndpointStatusFactory endpointStatusFactory;

    public List<EndpointDTO> getAllEndpointData() {

        List<EndpointDTO> endpointsDTO = endpointRepository.getRepositoryDTO();
        List<EndpointDTO> result = new ArrayList<>();

        for (EndpointDTO endpointDTO : endpointsDTO) {
            EndpointDTO info = new EndpointDTO();

            info.setId(endpointDTO.getId());
            info.setCallerid(endpointDTO.getCallerid());
            info.setUsername(endpointDTO.getUsername());
            info.setPassword(endpointDTO.getPassword());

            Endpoint endpoint = endpointFactory.getById(endpointDTO.getId());
            Optional<EndpointStatus> optional = endpointStatusFactory.getByEndpoint(endpoint);

            optional.ifPresentOrElse(
                    endpointStatus -> {
                        info.setOnline(endpointStatus.isOnline());
                        info.setIp(endpointStatus.getIp());
                        info.setRtt(endpointStatus.getRtt());
                    },
                    () -> info.setOnline(false));

            result.add(info);
        }
        return result;
    }
}
