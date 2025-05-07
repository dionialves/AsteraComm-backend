package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.repository.EndpointStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EndpointHistoryService {
    private EndpointStatusService endpointStatusService;
    private EndpointStatusRepository endpointStatusRepository;

    @Autowired
    public EndpointHistoryService(EndpointStatusService endpointStatusService, EndpointStatusRepository endpointStatusRepository) {
        this.endpointStatusService = endpointStatusService;
        this.endpointStatusRepository = endpointStatusRepository;
    }

    @Transactional
    public void saveStatusToDatabase() {
        Map<String, EndpointStatus> statusMap = endpointStatusService.getStatusFromAsterisk();

        for (Map.Entry<String, EndpointStatus> entry : statusMap.entrySet() ) {
            EndpointStatus entity = new EndpointStatus();

            entity.setOnline(entry.getValue().isOnline());
            entity.setIp(entry.getValue().getIp());
            entity.setRtt(entry.getValue().getRtt());
            entity.setCheckedAt(LocalDateTime.now());

            endpointStatusRepository.save(entity);
        }

    }
}
