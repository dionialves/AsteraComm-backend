package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointStatusRepository extends JpaRepository<EndpointStatus, Long> {
    Optional<EndpointStatus> findTopByEndpointOrderByCheckedAtDesc(Endpoint endpoint);
}
