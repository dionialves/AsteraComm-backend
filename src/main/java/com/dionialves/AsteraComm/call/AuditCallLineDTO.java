package com.dionialves.AsteraComm.call;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditCallLineDTO(
        String        uniqueId,
        LocalDateTime callDate,
        String        dst,
        CallType      callType,
        int           billSeconds,
        BigDecimal    ratePerMinute,
        int           quotaUsedThisCall,
        int           quotaAccumulated,
        BigDecimal    cost
) {}
