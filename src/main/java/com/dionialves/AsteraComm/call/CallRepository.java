package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.circuit.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long>,
        JpaSpecificationExecutor<Call> {

    Optional<Call> findByUniqueId(String uniqueId);

    @Query(value = "SELECT COALESCE(SUM(minutes_from_quota), 0) FROM asteracomm_calls " +
            "WHERE circuit_number = :circuitNumber " +
            "AND call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR  FROM call_date) = EXTRACT(YEAR  FROM CURRENT_DATE)", nativeQuery = true)
    int sumQuotaMinutesThisMonth(@Param("circuitNumber") String circuitNumber);

    @Query(value = "SELECT COALESCE(SUM(minutes_from_quota), 0) FROM asteracomm_calls " +
            "WHERE circuit_number = :circuitNumber " +
            "AND call_type = :callType " +
            "AND call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR  FROM call_date) = EXTRACT(YEAR  FROM CURRENT_DATE)", nativeQuery = true)
    int sumQuotaMinutesThisMonthByType(@Param("circuitNumber") String circuitNumber,
            @Param("callType") String callType);
}
