package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.dto.EndpointDTO;
import com.dionialves.AsteraComm.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EndpointRepository extends JpaRepository<Endpoint, String> {
    @Query("SELECT new com.dionialves.AsteraComm.dto.EndpointDTO(e.id, e.callerid, a.username, a.password) " +
            "FROM Endpoint e JOIN e.auth a")
    List<EndpointDTO> getRepositoryDTO();
}
