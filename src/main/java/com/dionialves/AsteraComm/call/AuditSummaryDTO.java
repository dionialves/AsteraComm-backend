package com.dionialves.AsteraComm.call;

import java.math.BigDecimal;

public record AuditSummaryDTO(
        int        totalCalls,
        int        totalMinutes,
        int        quotaMinutesUsed,
        int        excessMinutes,
        BigDecimal totalCost
) {}
