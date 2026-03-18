package com.dionialves.AsteraComm.did;

import com.dionialves.AsteraComm.circuit.Circuit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DIDRepository extends JpaRepository<DID, Long> {

    boolean existsByNumber(String number);

    boolean existsByCircuit(Circuit circuit);

    Page<DID> findByNumberContaining(String search, Pageable pageable);

    List<DID> findByCircuitIsNull();

    List<DID> findByCircuit_Number(String circuitNumber);

}
