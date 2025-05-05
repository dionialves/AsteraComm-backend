package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.repository.EndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EndpointService {

    @Autowired
    private EndpointRepository endpointRepository;

    @Autowired
    private EndpointStatusService endpointStatusService;

    public List<EndpointDTO> getAllEndpointData() {
        Map<String, EndpointStatus> liveStatus = endpointStatusService.getStatusFromAsterisk();
        List<EndpointDTO> endpointsDTO = endpointRepository.getRepositoryDTO();

        List<EndpointDTO> result = new ArrayList<>();

        for (EndpointDTO endpoint : endpointsDTO) {
            EndpointDTO info = new EndpointDTO();

            info.setId(endpoint.getId());
            info.setCallerid(endpoint.getCallerid());
            info.setUsername(endpoint.getUsername());
            info.setPassword(endpoint.getPassword());

            EndpointStatus status = liveStatus.get(endpoint.getId());
            if (status != null) {
                info.setOnline(status.isOnline());
                info.setIp(status.getIp());
                info.setRtt(status.getRtt());
            } else {
                info.setOnline(false);
            }
            result.add(info);
        }
        return result;
    }
}
