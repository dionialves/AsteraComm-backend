package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.dto.EndPointDTO;
import com.dionialves.AsteraComm.entity.EndPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EndPointRepository extends JpaRepository<EndPoint, String> {
    @Query("SELECT new com.dionialves.AsteraComm.dto.EndPointDTO(e.id, e.callerid, a.username, a.password) " +
            "FROM EndPoint e JOIN e.auth a")
    List<EndPointDTO> getRepositoryDTO();
}
