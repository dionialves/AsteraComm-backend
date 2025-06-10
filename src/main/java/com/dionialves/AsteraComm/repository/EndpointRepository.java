package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.projection.EndpointProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EndpointRepository extends JpaRepository<Endpoint, String> {
    @Query(value = """
            SELECT
                e.id AS id,
                e.callerid AS callerid,
                a.username AS username,
                a.password AS password,
                s.ip AS ip,
                s.rtt AS rtt,
                CASE WHEN s.id IS NOT NULL THEN true ELSE false END AS online
            FROM ps_endpoints e
            JOIN ps_auths a ON a.id = e.id
            LEFT JOIN asteracomm_endpoint_status_history s ON s.id = (
                SELECT MAX(s2.id)
                FROM asteracomm_endpoint_status_history s2
                WHERE s2.endpoint = e.id
            )
            WHERE LOWER(e.callerid) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, countQuery = """
            SELECT COUNT(*)
            FROM ps_endpoints e
            JOIN ps_auths a ON a.id = e.id
            LEFT JOIN asteracomm_endpoint_status_history s ON s.id = (
                SELECT MAX(s2.id)
                FROM asteracomm_endpoint_status_history s2
                WHERE s2.endpoint = e.id
            )
            WHERE LOWER(e.callerid) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, nativeQuery = true)

    Page<EndpointProjection> findAllEndpoint(@Param("search") String search, Pageable pageable);

}
