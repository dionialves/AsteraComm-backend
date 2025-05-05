package com.dionialves.AsteraComm.service;

import com.dionialves.AsteraComm.entity.EndpointStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class EndpointStatusService {

    public Map<String, EndpointStatus> getStatusFromAsterisk() {
        Map<String, EndpointStatus> statusMap = new HashMap<>();

        try {
            Process process = Runtime.getRuntime().exec(new String[] {
                    "sudo", "asterisk", "-rx", "pjsip show contacts"
            });
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.contains("Avail") & line.contains("@")) {

                    String[] parts = line.trim().split("\\s+");

                    String endpoint = parts[1].split("/")[0];
                    String ip = parts[1].split("/")[1].split("@")[1].split(":")[0];
                    String rtt = parts[parts.length - 1];

                    statusMap.put(endpoint, new EndpointStatus(true, ip, rtt));

                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusMap;
    }
}
