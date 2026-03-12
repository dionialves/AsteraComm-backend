package com.dionialves.AsteraComm.cdr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CdrRepository extends JpaRepository<CdrRecord, Long>,
        JpaSpecificationExecutor<CdrRecord> {

    Optional<CdrRecord> findByUniqueId(String uniqueId);
}
