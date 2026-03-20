package com.dionialves.AsteraComm.report;

import java.util.List;

public record CostPerCircuitResponseDTO(
        CostPerCircuitSummaryDTO summary,
        List<CallCostReportDTO> data) {}
