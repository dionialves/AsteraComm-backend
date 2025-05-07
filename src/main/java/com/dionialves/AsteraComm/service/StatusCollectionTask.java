package com.dionialves.AsteraComm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusCollectionTask {
    private final EndpointHistoryService endpointHistoryService;

    @Autowired
    public StatusCollectionTask(EndpointHistoryService endpointHistoryService) {
        this.endpointHistoryService = endpointHistoryService;
    }

    @Scheduled(fixedRate = 60000)  // A cada 1 minuto
    public void collectAndSaveStatus() {
        endpointHistoryService.saveStatusToDatabase();
    }
}
