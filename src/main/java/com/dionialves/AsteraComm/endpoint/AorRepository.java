package com.dionialves.AsteraComm.endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AorRepository extends JpaRepository<Aors, String> {
}
