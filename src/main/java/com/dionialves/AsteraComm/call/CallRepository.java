package com.dionialves.AsteraComm.call;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long>,
        JpaSpecificationExecutor<Call> {

    Optional<Call> findByUniqueId(String uniqueId);
}
