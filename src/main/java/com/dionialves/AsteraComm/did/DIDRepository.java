package com.dionialves.AsteraComm.did;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DIDRepository extends JpaRepository<DID, Long> {

    boolean existsByNumber(String number);

    boolean existsByCircuitNumber(String circuitNumber);
}
