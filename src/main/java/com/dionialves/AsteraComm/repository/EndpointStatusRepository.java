package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.entity.EndpointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointStatusRepository extends JpaRepository<EndpointStatus, Long> {
}
