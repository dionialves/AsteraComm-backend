package com.dionialves.AsteraComm.auth;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByUsername(String username);

    Optional<User> findUserByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String name, String username, Pageable pageable);
}
