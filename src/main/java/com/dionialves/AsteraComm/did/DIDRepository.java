package com.dionialves.AsteraComm.did;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DIDRepository extends JpaRepository<DID, Long> {

    boolean existsByNumber(String number);

    boolean existsByCircuitNumber(String circuitNumber);

    Page<DID> findByNumberContaining(String search, Pageable pageable);

    List<DID> findByCircuitNumberIsNull();
}
