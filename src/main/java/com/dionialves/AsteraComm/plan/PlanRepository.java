package com.dionialves.AsteraComm.plan;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByName(String name);

    // Returns false until client entity is implemented
    default boolean isLinkedToClient(Long id) {
        return false;
    }
}
