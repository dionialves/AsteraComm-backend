package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.circuit.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long>,
        JpaSpecificationExecutor<Call> {

    Optional<Call> findByUniqueId(String uniqueId);

    @Query(value = "SELECT * FROM asteracomm_calls " +
            "WHERE circuit_number = :circuitNumber " +
            "AND call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year " +
            "ORDER BY call_date ASC", nativeQuery = true)
    List<Call> findByCircuitNumberAndPeriod(@Param("circuitNumber") String circuitNumber,
                                            @Param("month") int month,
                                            @Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(minutes_from_quota), 0) FROM asteracomm_calls " +
            "WHERE circuit_number = :circuitNumber " +
            "AND call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    int sumQuotaMinutes(@Param("circuitNumber") String circuitNumber,
                        @Param("month") int month,
                        @Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(minutes_from_quota), 0) FROM asteracomm_calls " +
            "WHERE circuit_number = :circuitNumber " +
            "AND call_type = :callType " +
            "AND call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    int sumQuotaMinutesByType(@Param("circuitNumber") String circuitNumber,
                              @Param("callType") String callType,
                              @Param("month") int month,
                              @Param("year") int year);
}
