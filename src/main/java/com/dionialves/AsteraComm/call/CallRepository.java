package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.circuit.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long>,
        JpaSpecificationExecutor<Call> {

    Optional<Call> findByUniqueId(String uniqueId);

    @Query(value = "SELECT COUNT(*) FROM asteracomm_calls " +
            "WHERE call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    long countByPeriod(@Param("month") int month, @Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(bill_seconds), 0) FROM asteracomm_calls " +
            "WHERE call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    long sumBillSecondsByPeriod(@Param("month") int month, @Param("year") int year);

    @Query(value = "SELECT COUNT(*) FROM asteracomm_calls " +
            "WHERE call_status = 'PROCESSED' " +
            "AND UPPER(disposition) = UPPER(:disposition) " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    long countByDispositionAndPeriod(@Param("disposition") String disposition,
                                     @Param("month") int month,
                                     @Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(cost), 0) FROM asteracomm_calls " +
            "WHERE call_status = 'PROCESSED' " +
            "AND EXTRACT(MONTH FROM call_date) = :month " +
            "AND EXTRACT(YEAR  FROM call_date) = :year", nativeQuery = true)
    BigDecimal sumCostByPeriod(@Param("month") int month, @Param("year") int year);

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

    @Query(value = """
            SELECT
                DATE(call_date) AS day,
                COALESCE(SUM(CASE WHEN UPPER(disposition) = 'ANSWERED'  THEN 1 ELSE 0 END), 0) AS answered,
                COALESCE(SUM(CASE WHEN UPPER(disposition) = 'NO ANSWER' THEN 1 ELSE 0 END), 0) AS no_answer,
                COALESCE(SUM(CASE WHEN UPPER(disposition) = 'BUSY'      THEN 1 ELSE 0 END), 0) AS busy,
                COALESCE(SUM(CASE WHEN UPPER(disposition) = 'FAILED'    THEN 1 ELSE 0 END), 0) AS failed
            FROM asteracomm_calls
            WHERE call_status = 'PROCESSED'
            AND call_date >= :startDate
            GROUP BY DATE(call_date)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> findDailyCallStats(@Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT
                EXTRACT(YEAR  FROM call_date)::int AS year,
                EXTRACT(MONTH FROM call_date)::int AS month,
                COALESCE(SUM(cost), 0)             AS total_cost
            FROM asteracomm_calls
            WHERE call_status = 'PROCESSED'
            AND call_date >= :startDate
            GROUP BY EXTRACT(YEAR FROM call_date), EXTRACT(MONTH FROM call_date)
            ORDER BY year, month
            """, nativeQuery = true)
    List<Object[]> findMonthlyCallCosts(@Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT
                c.number                                        AS circuit_number,
                cu.name                                         AS customer_name,
                p.name                                          AS plan_name,
                COALESCE(SUM(ca.minutes_from_quota), 0)::bigint AS used_minutes,
                p.package_total_minutes::bigint                 AS limit_minutes
            FROM asteracomm_circuits c
            JOIN asteracomm_customers cu ON cu.id = c.customer_id
            JOIN asteracomm_plans p ON p.id = c.plan_id
            LEFT JOIN asteracomm_calls ca ON ca.circuit_number = c.number
                AND ca.call_status = 'PROCESSED'
                AND EXTRACT(MONTH FROM ca.call_date) = :month
                AND EXTRACT(YEAR  FROM ca.call_date) = :year
            WHERE p.package_total_minutes IS NOT NULL
            AND p.package_total_minutes > 0
            GROUP BY c.number, cu.name, p.name, p.package_total_minutes
            ORDER BY used_minutes DESC
            """, nativeQuery = true)
    List<Object[]> findCircuitConsumption(@Param("month") int month, @Param("year") int year);
}
