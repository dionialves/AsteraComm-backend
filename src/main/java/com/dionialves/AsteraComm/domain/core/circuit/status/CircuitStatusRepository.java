package com.dionialves.AsteraComm.domain.core.circuit.status;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.Endpoint;

@Repository
public interface CircuitStatusRepository extends JpaRepository<CircuitStatus, Long> {
    Optional<CircuitStatus> findTopByEndpointOrderByCheckedAtDesc(Endpoint endpoint);

    void deleteByEndpoint(Endpoint endpoint);
}
