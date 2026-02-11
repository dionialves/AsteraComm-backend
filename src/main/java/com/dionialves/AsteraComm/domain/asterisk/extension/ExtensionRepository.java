package com.dionialves.AsteraComm.domain.asterisk.extension;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.Endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtensionRepository extends JpaRepository<Extension, Long> {
    void deleteByExten(Endpoint exten);
}
