package com.dionialves.AsteraComm.repository;

import com.dionialves.AsteraComm.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String> {
}
