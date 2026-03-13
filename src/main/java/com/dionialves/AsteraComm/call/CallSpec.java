package com.dionialves.AsteraComm.call;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CallSpec {

    private CallSpec() {}

    public static Specification<Call> withFilters(String callerNumber, String dst, String disposition,
                                                  LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (callerNumber != null && !callerNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("callerNumber")), "%" + callerNumber.toLowerCase() + "%"));
            }
            if (dst != null && !dst.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("dst")), "%" + dst.toLowerCase() + "%"));
            }
            if (disposition != null && !disposition.isBlank()) {
                predicates.add(cb.equal(root.get("disposition"), disposition));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("callDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("callDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
