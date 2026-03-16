package com.dionialves.AsteraComm.report;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class CallReportController {

    private final CallReportService callReportService;

    @GetMapping("/call-cost")
    public List<CallCostReportDTO> getCallCostReport(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "false") boolean onlyWithCost) {
        return callReportService.getReport(month, year, onlyWithCost);
    }
}
