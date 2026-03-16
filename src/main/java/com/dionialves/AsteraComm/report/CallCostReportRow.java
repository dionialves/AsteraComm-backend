package com.dionialves.AsteraComm.report;

import java.math.BigDecimal;

public record CallCostReportRow(
        String circuitNumber,
        String customerName,
        Long callCount,
        Long totalBillSeconds,
        BigDecimal totalCost
) {}
