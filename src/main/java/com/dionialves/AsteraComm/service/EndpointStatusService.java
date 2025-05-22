package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.entity.EndpointStatus;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, EndpointStatus> getStatusFromAsterisk() {
        Map<String, EndpointStatus> statusMap = new HashMap<>();

        ManagerConnectionFactory factory = new ManagerConnectionFactory(hostname, port, username, password);
        ManagerConnection connection = factory.createManagerConnection();

        try {
            connection.login();

            CommandAction action = new CommandAction("pjsip show contacts");
            CommandResponse response = (CommandResponse) connection.sendAction(action);

            for (String line : response.getResult()) {

                if (line.contains("Avail") & line.contains("@")) {

                    String[] parts = line.trim().split("\\s+");

                    String endpoint = parts[1].split("/")[0];
                    String ip = parts[1].split("/")[1].split("@")[1].split(":")[0];
                    String rtt = parts[parts.length - 1];

                    statusMap.put(endpoint, new EndpointStatus(true, ip, rtt));

                }

            }
            connection.logoff();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusMap;
    }
}
