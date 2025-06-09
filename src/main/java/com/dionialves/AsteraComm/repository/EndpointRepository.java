package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.entity.Endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EndpointRepository extends JpaRepository<Endpoint, String> {
    @Query(value = """
                SELECT
                    e.id,
                    e.callerid,
                    a.username,
                    a.password,
                    s.ip,
                    s.rtt,
                    CASE WHEN s.id IS NOT NULL THEN true ELSE false END AS online
                FROM ps_endpoints e
                JOIN ps_auths a ON a.id = e.id
                LEFT JOIN asteracomm_endpoint_status_history s ON s.id = (
                    SELECT MAX(s2.id)
                    FROM asteracomm_endpoint_status_history s2
                    WHERE s2.endpoint = e.id
                )
            """, nativeQuery = true)
    List<EndpointDTO> getRepositoryDTO();
}
