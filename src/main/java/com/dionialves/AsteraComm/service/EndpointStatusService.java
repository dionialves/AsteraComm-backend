package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.factory.EndpointFactory;
import com.dionialves.AsteraComm.repository.EndpointStatusRepository;

import java.time.LocalDateTime;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EndpointStatusService {

    @Value("${asterisk.hostname}")
    private String hostname;

    @Value("${asterisk.port}")
    private int port;

    @Value("${asterisk.username}")
    private String username;

    @Value("${asterisk.password}")
    private String password;

    private final EndpointFactory endpointFactory;
    private final EndpointStatusRepository endpointStatusRepository;

    @Autowired
    public EndpointStatusService(EndpointFactory endpointFactory, EndpointStatusRepository endpointStatusRepository) {
        this.endpointFactory = endpointFactory;
        this.endpointStatusRepository = endpointStatusRepository;
    }

    @Scheduled(fixedRateString = "${asterisk.status.interval.ms}")
    public void getStatusFromAsterisk() {

        ManagerConnectionFactory factory = new ManagerConnectionFactory(hostname, port, username, password);
        ManagerConnection connection = factory.createManagerConnection();

        try {
            connection.login();

            CommandAction action = new CommandAction("pjsip show contacts");
            CommandResponse response = (CommandResponse) connection.sendAction(action);

            for (String line : response.getResult()) {

                if (line.contains("@")) {

                    String[] parts = line.trim().split("\\s+");

                    boolean status = line.contains("Avail");

                    String endpointId = parts[1].split("/")[0];
                    String ip = parts[1].split("/")[1].split("@")[1].split(":")[0];
                    String rtt = parts[parts.length - 1];

                    LocalDateTime now = LocalDateTime.now();

                    Endpoint endpoint = endpointFactory.getById(endpointId);

                    EndpointStatus endpointStatus = new EndpointStatus(endpoint, status, ip, rtt, now);
                    endpointStatusRepository.save(endpointStatus);
                }
            }
            connection.logoff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
