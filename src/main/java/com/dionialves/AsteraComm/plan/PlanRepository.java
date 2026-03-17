package com.dionialves.AsteraComm.plan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByName(String name);

    Page<Plan> findByNameContainingIgnoreCase(String search, Pageable pageable);

    // Returns false until client entity is implemented
    default boolean isLinkedToClient(Long id) {
        return false;
    }
}
