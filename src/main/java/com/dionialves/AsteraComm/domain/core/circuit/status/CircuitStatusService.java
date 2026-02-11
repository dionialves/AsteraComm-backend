package com.dionialves.AsteraComm.domain.core.circuit.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.domain.asterisk.endpoint.EndpointFactory;

@Service
public class CircuitStatusService {

    @Value("${asterisk.hostname}")
    private String hostname;

    @Value("${asterisk.port}")
    private int port;

    @Value("${asterisk.username}")
    private String username;

    @Value("${asterisk.password}")
    private String password;

    private final EndpointFactory endpointFactory;
    private final CircuitStatusRepository endpointStatusRepository;

    @Autowired
    public CircuitStatusService(EndpointFactory endpointFactory, CircuitStatusRepository endpointStatusRepository) {
        this.endpointFactory = endpointFactory;
        this.endpointStatusRepository = endpointStatusRepository;
    }

    @Scheduled(fixedRateString = "${asterisk.status.interval.ms}")
    public void getStatusFromAsterisk() {
        CommandResponse response = this.getContactsFromAsterisk();

        if (response == null) {
            System.err.println("Failed to get response from asterisk");
        }

        List<CircuitStatus> listEndpointStatus = this.getEndpointStatus(response.getResult());

        this.saveEndpointStatus(listEndpointStatus);
    }

    private CommandResponse getContactsFromAsterisk() {

        try {
            ManagerConnectionFactory factory = new ManagerConnectionFactory(hostname, port, username, password);
            ManagerConnection connection = factory.createManagerConnection();

            connection.login();
            CommandAction action = new CommandAction("pjsip show contacts");
            CommandResponse response = (CommandResponse) connection.sendAction(action);
            connection.logoff();

            return response;

        } catch (IOException | AuthenticationFailedException | TimeoutException e) {
            System.err.println("Error on connecting Asterisk: " + e.getMessage());
            return null;
        }
    }

    private List<CircuitStatus> getEndpointStatus(List<String> response) {

        List<CircuitStatus> listEndpointStatus = new ArrayList<>();

        for (String line : response) {

            if (!line.contains("@"))
                continue;

            try {

                String[] parts = line.trim().split("\\s+");

                String endpointId = parts[1].split("/")[0];
                Endpoint endpoint = endpointFactory.getById(endpointId);
                boolean status = line.contains("Avail");
                String ip = parts[1].split("/")[1].split("@")[1].split(":")[0];
                String rtt = parts[parts.length - 1];
                LocalDateTime now = LocalDateTime.now();

                listEndpointStatus.add(new CircuitStatus(endpoint, status, ip, rtt, now));
            } catch (Exception e) {
                System.err.println("Error processing line: " + line);
            }
        }

        return listEndpointStatus;
    }

    private void saveEndpointStatus(List<CircuitStatus> listEndpointStatus) {

        endpointStatusRepository.deleteAll();

        for (CircuitStatus endpointStatus : listEndpointStatus) {
            endpointStatusRepository.save(endpointStatus);
        }
    }
}
