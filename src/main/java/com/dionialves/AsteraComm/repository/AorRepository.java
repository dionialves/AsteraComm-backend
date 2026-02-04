package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.entity.Aor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AorRepository extends JpaRepository<Aor, String> {
}
