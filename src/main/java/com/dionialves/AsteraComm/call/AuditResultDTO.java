package com.dionialves.AsteraComm.call;

import java.util.List;

public record AuditResultDTO(
        String               circuitNumber,
        String               planName,
        int                  month,
        int                  year,
        List<AuditCallLineDTO> lines,
        AuditSummaryDTO      summary
) {}
