package com.dionialves.AsteraComm.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CallReportService {

    private final CallReportRepository callReportRepository;

    public List<CallCostReportDTO> getReport(int month, int year, boolean onlyWithCost) {
        return callReportRepository.findCallCostByPeriod(month, year).stream()
                .filter(row -> !onlyWithCost || row.totalCost().compareTo(BigDecimal.ZERO) > 0)
                .map(row -> new CallCostReportDTO(
                        row.customerName(),
                        row.circuitNumber(),
                        row.callCount(),
                        row.totalBillSeconds() / 60,
                        row.totalCost()
                ))
                .toList();
    }
}
