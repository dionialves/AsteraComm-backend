package com.dionialves.AsteraComm.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallReportServiceTest {

    @Mock
    private CallReportRepository callReportRepository;

    @InjectMocks
    private CallReportService callReportService;

    private CallCostReportRow buildRow(String circuitNumber, String customerName,
                                       long callCount, long totalBillSeconds,
                                       BigDecimal totalCost) {
        return new CallCostReportRow(circuitNumber, customerName, callCount, totalBillSeconds, totalCost);
    }

    // -------------------------------------------------------------------------
    // onlyWithCost = false
    // -------------------------------------------------------------------------

    @Test
    void getReport_returnsAllCircuits_whenOnlyWithCostIsFalse() {
        CallCostReportRow rowA = buildRow("1001", "Cliente X", 5, 600, new BigDecimal("1.50"));
        CallCostReportRow rowB = buildRow("1002", "Cliente Y", 3, 180, BigDecimal.ZERO);
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(rowA, rowB));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, false);

        assertThat(result).hasSize(2);
        verify(callReportRepository).findCallCostByPeriod(3, 2026);
    }

    @Test
    void getReport_mapsRowToDTO_correctly() {
        CallCostReportRow row = buildRow("1001", "Cliente X", 5, 600, new BigDecimal("1.50"));
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(row));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, false);

        CallCostReportDTO dto = result.get(0);
        assertThat(dto.customerName()).isEqualTo("Cliente X");
        assertThat(dto.circuitName()).isEqualTo("1001");
        assertThat(dto.callCount()).isEqualTo(5);
        assertThat(dto.totalMinutes()).isEqualTo(10); // 600s / 60
        assertThat(dto.totalCost()).isEqualByComparingTo(new BigDecimal("1.50"));
    }

    @Test
    void getReport_returnsEmptyList_whenNoCallsInPeriod() {
        when(callReportRepository.findCallCostByPeriod(1, 2026)).thenReturn(List.of());

        List<CallCostReportDTO> result = callReportService.getReport(1, 2026, false);

        assertThat(result).isEmpty();
    }

    @Test
    void getReport_returnsMultipleCircuits_withDistinctCosts() {
        CallCostReportRow rowA = buildRow("1001", "Cliente X", 10, 1800, new BigDecimal("4.50"));
        CallCostReportRow rowB = buildRow("1002", "Cliente Y",  2,  120, new BigDecimal("0.27"));
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(rowA, rowB));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, false);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).totalCost()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(result.get(1).totalCost()).isEqualByComparingTo(new BigDecimal("0.27"));
    }

    // -------------------------------------------------------------------------
    // onlyWithCost = true
    // -------------------------------------------------------------------------

    @Test
    void getReport_excludesZeroCostCircuits_whenOnlyWithCostIsTrue() {
        CallCostReportRow rowWithCost    = buildRow("1001", "Cliente X", 5, 600, new BigDecimal("1.50"));
        CallCostReportRow rowWithoutCost = buildRow("1002", "Cliente Y", 3, 180, BigDecimal.ZERO);
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(rowWithCost, rowWithoutCost));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).circuitName()).isEqualTo("1001");
    }

    @Test
    void getReport_returnsAll_whenOnlyWithCostIsTrueAndAllHaveCost() {
        CallCostReportRow rowA = buildRow("1001", "Cliente X", 5, 600, new BigDecimal("1.50"));
        CallCostReportRow rowB = buildRow("1002", "Cliente Y", 2, 120, new BigDecimal("0.27"));
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(rowA, rowB));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, true);

        assertThat(result).hasSize(2);
    }

    @Test
    void getReport_returnsEmpty_whenOnlyWithCostIsTrueAndNoneHaveCost() {
        CallCostReportRow row = buildRow("1001", "Cliente X", 3, 180, BigDecimal.ZERO);
        when(callReportRepository.findCallCostByPeriod(3, 2026)).thenReturn(List.of(row));

        List<CallCostReportDTO> result = callReportService.getReport(3, 2026, true);

        assertThat(result).isEmpty();
    }
}
