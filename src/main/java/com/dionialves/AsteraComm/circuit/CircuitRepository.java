package com.dionialves.AsteraComm.circuit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CircuitRepository extends JpaRepository<Circuit, String> {

    @Query("SELECT MAX(c.number) FROM Circuit c")
    Optional<String> findMaxCode();

    @Query(value = """
            WITH last_status AS (
                SELECT DISTINCT ON (endpoint) *
                FROM asteracomm_endpoint_status
                WHERE online = true
                ORDER BY endpoint, id DESC
            )
            SELECT
                c.number AS id,
                c.password AS password,
                c.trunk_name AS trunkName,
                s.ip AS ip,
                s.rtt AS rtt,
                CASE WHEN s.id IS NOT NULL THEN true ELSE false END AS online
            FROM asteracomm_circuits c
            LEFT JOIN last_status s ON s.endpoint = c.number
            WHERE LOWER(c.number) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, countQuery = """
            WITH last_status AS (
                SELECT DISTINCT ON (endpoint) *
                FROM asteracomm_endpoint_status
                WHERE online = true
                ORDER BY endpoint, id DESC
            )
            SELECT COUNT(*)
            FROM asteracomm_circuits c
            LEFT JOIN last_status s ON s.endpoint = c.number
            WHERE LOWER(c.number) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.ip) LIKE LOWER(CONCAT('%', :search, '%'))
            """, nativeQuery = true)
    Page<CircuitProjection> findAllCircuits(@Param("search") String search, Pageable pageable);
}
