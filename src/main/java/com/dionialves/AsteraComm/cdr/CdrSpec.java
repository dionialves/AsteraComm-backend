package com.dionialves.AsteraComm.cdr;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CdrSpec {

    private CdrSpec() {}

    public static Specification<CdrRecord> withFilters(String src, String dst, String disposition,
                                                       LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (src != null && !src.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("src")), "%" + src.toLowerCase() + "%"));
            }
            if (dst != null && !dst.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("dst")), "%" + dst.toLowerCase() + "%"));
            }
            if (disposition != null && !disposition.isBlank()) {
                predicates.add(cb.equal(root.get("disposition"), disposition));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("calldate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("calldate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
