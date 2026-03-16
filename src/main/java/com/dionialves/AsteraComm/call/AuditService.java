package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.exception.NotFoundException;
import com.dionialves.AsteraComm.plan.PackageType;
import com.dionialves.AsteraComm.plan.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuditService {

    private final CircuitRepository circuitRepository;
    private final CallRepository    callRepository;

    public AuditResultDTO simulate(String circuitNumber, int month, int year) {
        Circuit circuit = circuitRepository.findById(circuitNumber)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado: " + circuitNumber));

        Plan plan = circuit.getPlan();
        if (plan == null) {
            throw new NotFoundException("Circuito " + circuitNumber + " não possui plano vinculado");
        }

        List<Call> calls = callRepository.findByCircuitNumberAndPeriod(circuitNumber, month, year);

        return buildResult(circuit, plan, month, year, calls);
    }

    private AuditResultDTO buildResult(Circuit circuit, Plan plan, int month, int year, List<Call> calls) {
        List<AuditCallLineDTO> lines = new ArrayList<>();

        // Acumuladores de quota (independentes por tipo em PER_CATEGORY)
        int quotaAccumulated = 0;
        int unifiedRemaining = resolveUnifiedQuota(plan);
        Map<CallType, Integer> perCategoryRemaining = resolvePerCategoryQuota(plan);

        // Totalizadores para o resumo
        int totalMinutes      = 0;
        int quotaMinutesUsed  = 0;
        BigDecimal totalCost  = BigDecimal.ZERO;

        for (Call call : calls) {
            int billSeconds       = call.getBillSeconds();
            CallType callType     = call.getCallType();
            BigDecimal rate       = resolveRate(plan, callType);
            int durationFractions = (int) Math.ceil(billSeconds / 30.0);

            BigDecimal cost;
            int quotaUsedThisCall;

            if (billSeconds <= 3) {
                cost              = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
                quotaUsedThisCall = 0;
            } else if (plan.getPackageType() == PackageType.UNIFIED) {
                quotaUsedThisCall = applyQuota(unifiedRemaining, durationFractions);
                cost              = decodeCost(billSeconds, rate, durationFractions, unifiedRemaining);
                unifiedRemaining -= quotaUsedThisCall;
            } else if (plan.getPackageType() == PackageType.PER_CATEGORY) {
                int categoryRemaining = perCategoryRemaining.getOrDefault(callType, 0);
                quotaUsedThisCall = applyQuota(categoryRemaining, durationFractions);
                cost              = decodeCost(billSeconds, rate, durationFractions, categoryRemaining);
                perCategoryRemaining.merge(callType, -quotaUsedThisCall, Integer::sum);
            } else {
                // PackageType.NONE
                quotaUsedThisCall = 0;
                cost              = CallCostingService.calculateFractionCost(billSeconds, rate);
            }

            quotaAccumulated += quotaUsedThisCall;
            if (billSeconds > 3) totalMinutes += durationFractions;
            quotaMinutesUsed += quotaUsedThisCall;
            totalCost         = totalCost.add(cost);

            lines.add(new AuditCallLineDTO(
                    call.getUniqueId(),
                    call.getCallDate(),
                    call.getDst(),
                    callType,
                    billSeconds,
                    rate,
                    quotaUsedThisCall,
                    quotaAccumulated,
                    cost
            ));
        }

        int excessMinutes = totalMinutes - quotaMinutesUsed;

        AuditSummaryDTO summary = new AuditSummaryDTO(
                lines.size(),
                totalMinutes,
                quotaMinutesUsed,
                excessMinutes,
                totalCost
        );

        return new AuditResultDTO(circuit.getNumber(), plan.getName(), month, year, lines, summary);
    }

    private static BigDecimal decodeCost(int billSeconds, BigDecimal rate,
                                         int durationFractions, int remaining) {
        if (remaining >= durationFractions) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        } else if (remaining > 0) {
            int billableSeconds = billSeconds - (remaining * 30);
            return CallCostingService.calculateFractionCost(billableSeconds, rate);
        } else {
            return CallCostingService.calculateFractionCost(billSeconds, rate);
        }
    }

    private static int applyQuota(int remaining, int durationFractions) {
        if (remaining >= durationFractions) return durationFractions;
        if (remaining > 0)                 return remaining;
        return 0;
    }

    private int resolveUnifiedQuota(Plan plan) {
        if (plan.getPackageType() != PackageType.UNIFIED) return 0;
        return plan.getPackageTotalMinutes() != null ? plan.getPackageTotalMinutes() * 2 : 0;
    }

    private Map<CallType, Integer> resolvePerCategoryQuota(Plan plan) {
        Map<CallType, Integer> map = new EnumMap<>(CallType.class);
        if (plan.getPackageType() != PackageType.PER_CATEGORY) return map;
        putIfNotNull(map, CallType.FIXED_LOCAL,          plan.getPackageFixedLocal());
        putIfNotNull(map, CallType.FIXED_LONG_DISTANCE,  plan.getPackageFixedLongDistance());
        putIfNotNull(map, CallType.MOBILE_LOCAL,         plan.getPackageMobileLocal());
        putIfNotNull(map, CallType.MOBILE_LONG_DISTANCE, plan.getPackageMobileLongDistance());
        return map;
    }

    private void putIfNotNull(Map<CallType, Integer> map, CallType type, Integer value) {
        if (value != null && value > 0) map.put(type, value * 2);
    }

    private static BigDecimal resolveRate(Plan plan, CallType callType) {
        return switch (callType) {
            case FIXED_LOCAL          -> plan.getFixedLocal();
            case FIXED_LONG_DISTANCE  -> plan.getFixedLongDistance();
            case MOBILE_LOCAL         -> plan.getMobileLocal();
            case MOBILE_LONG_DISTANCE -> plan.getMobileLongDistance();
            default                   -> BigDecimal.ZERO;
        };
    }
}
