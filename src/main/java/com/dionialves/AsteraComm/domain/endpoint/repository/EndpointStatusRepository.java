package com.dionialves.AsteraComm.domain.endpoint.repository;

import java.util.Optional;

import com.dionialves.AsteraComm.domain.endpoint.entity.EndpointStatus;
import com.dionialves.AsteraComm.domain.endpoint.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointStatusRepository extends JpaRepository<EndpointStatus, Long> {
    Optional<EndpointStatus> findTopByEndpointOrderByCheckedAtDesc(Endpoint endpoint);
}
