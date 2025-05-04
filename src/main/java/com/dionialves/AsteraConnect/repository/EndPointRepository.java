package com.dionialves.AsteraConnect.repository;

import com.dionialves.AsteraConnect.dto.EndPointDTO;
import com.dionialves.AsteraConnect.entity.EndPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EndPointRepository extends JpaRepository<EndPoint, String> {
    @Query("SELECT new com.dionialves.AsteraConnect.dto.EndPointDTO(e.id, e.callerid, a.username, a.password) " +
            "FROM EndPoint e JOIN e.auth a")
    List<EndPointDTO> getRepositoryDTO();
}
