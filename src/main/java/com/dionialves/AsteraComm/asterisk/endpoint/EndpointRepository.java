package com.dionialves.AsteraComm.asterisk.endpoint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EndpointRepository extends JpaRepository<Endpoint, String> {
    @Query(value = """
            WITH last_status AS (
                SELECT DISTINCT ON (endpoint) *
                FROM asteracomm_endpoint_status
                WHERE online = true
                ORDER BY endpoint, id DESC
            )
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
            LEFT JOIN last_status s ON s.endpoint = e.id
            WHERE LOWER(e.callerid) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, countQuery = """
            WITH last_status AS (
                SELECT DISTINCT ON (endpoint) *
                FROM asteracomm_endpoint_status
                WHERE online = true
                ORDER BY endpoint, id DESC
            )
            SELECT COUNT(*)
            FROM ps_endpoints e
            JOIN ps_auths a ON a.id = e.id
            LEFT JOIN last_status s ON s.endpoint = e.id
            WHERE LOWER(e.callerid) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, nativeQuery = true)

    Page<EndpointProjection> findAllEndpoint(@Param("search") String search, Pageable pageable);

}
