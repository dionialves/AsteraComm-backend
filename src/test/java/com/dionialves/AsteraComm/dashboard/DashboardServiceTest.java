package com.dionialves.AsteraComm.dashboard;

import com.dionialves.AsteraComm.call.CallRepository;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.trunk.TrunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CircuitRepository circuitRepository;

    @Mock
    private TrunkRepository trunkRepository;

    @Mock
    private CallRepository callRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private final LocalDate now = LocalDate.now();
    private final int currentMonth = now.getMonthValue();
    private final int currentYear = now.getYear();
    private final int prevMonth = now.minusMonths(1).getMonthValue();
    private final int prevYear = now.minusMonths(1).getYear();

    // -------------------------------------------------------------------------
    // Painel de Circuitos
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_circuitStats_totalAndOnlineAreCorrect() {
        stubCircuits(5L, 3L);
        stubTrunks(2L, 1L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuits().total()).isEqualTo(5);
        assertThat(result.circuits().online()).isEqualTo(3);
        assertThat(result.circuits().offline()).isEqualTo(2);
    }

    @Test
    void getDashboard_circuitStats_allOfflineWhenNoneOnline() {
        stubCircuits(4L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuits().online()).isEqualTo(0);
        assertThat(result.circuits().offline()).isEqualTo(4);
    }

    @Test
    void getDashboard_circuitStats_allOnlineWhenNoneOffline() {
        stubCircuits(3L, 3L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuits().online()).isEqualTo(3);
        assertThat(result.circuits().offline()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Painel de Troncos
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_trunkStats_totalAndRegisteredAreCorrect() {
        stubCircuits(0L, 0L);
        stubTrunks(4L, 2L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.trunks().total()).isEqualTo(4);
        assertThat(result.trunks().registered()).isEqualTo(2);
        assertThat(result.trunks().unregistered()).isEqualTo(2);
    }

    @Test
    void getDashboard_trunkStats_allUnregisteredWhenNoneRegistered() {
        stubCircuits(0L, 0L);
        stubTrunks(3L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.trunks().registered()).isEqualTo(0);
        assertThat(result.trunks().unregistered()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // Painel de Ligações
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_callStats_totalAndMinutesAreCorrect() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        // 120 calls, 7200 bill-seconds (= 120 minutes)
        stubCalls(120L, 7200L, 90L, 20L, 10L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.calls().total()).isEqualTo(120);
        assertThat(result.calls().totalMinutes()).isEqualTo(120);
    }

    @Test
    void getDashboard_callStats_minutesRoundedDown() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        // 90 bill-seconds = 1 full minute (floor)
        stubCalls(1L, 90L, 1L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.calls().totalMinutes()).isEqualTo(1);
    }

    @Test
    void getDashboard_callStats_dispositionBreakdown() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(50L, 3000L, 40L, 7L, 3L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.calls().answered()).isEqualTo(40);
        assertThat(result.calls().noAnswer()).isEqualTo(7);
        assertThat(result.calls().busy()).isEqualTo(3);
    }

    @Test
    void getDashboard_callStats_allZerosWhenNoCallsInPeriod() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.calls().total()).isEqualTo(0);
        assertThat(result.calls().totalMinutes()).isEqualTo(0);
        assertThat(result.calls().answered()).isEqualTo(0);
        assertThat(result.calls().noAnswer()).isEqualTo(0);
        assertThat(result.calls().busy()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Painel de Faturamento
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_billingStats_currentAndPreviousMonthCostsAreCorrect() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L,
                new BigDecimal("450.00"),
                new BigDecimal("380.00"));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.billing().currentMonthCost())
                .isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(result.billing().previousMonthCost())
                .isEqualByComparingTo(new BigDecimal("380.00"));
    }

    @Test
    void getDashboard_billingStats_zeroWhenNoCostsInPeriod() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.billing().currentMonthCost())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.billing().previousMonthCost())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getDashboard_billingStats_queriesCorrectMonthsForCurrentAndPrevious() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);

        dashboardService.getDashboard();

        verify(callRepository).sumCostByPeriod(currentMonth, currentYear);
        verify(callRepository).sumCostByPeriod(prevMonth, prevYear);
    }

    // -------------------------------------------------------------------------
    // Cobertura do período correto nas queries de ligações
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_callStats_queriesCurrentMonthAndYear() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(10L, 600L, 8L, 1L, 1L, BigDecimal.ZERO, BigDecimal.ZERO);

        dashboardService.getDashboard();

        verify(callRepository).countByPeriod(currentMonth, currentYear);
        verify(callRepository).sumBillSecondsByPeriod(currentMonth, currentYear);
        verify(callRepository).countByDispositionAndPeriod("ANSWERED", currentMonth, currentYear);
        verify(callRepository).countByDispositionAndPeriod("NO ANSWER", currentMonth, currentYear);
        verify(callRepository).countByDispositionAndPeriod("BUSY", currentMonth, currentYear);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Near-limit circuits — exclusão de excedidos e ordenação
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_nearLimitCircuits_excludesExceededCircuits() {
        stubCircuits(2L, 2L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(callRepository.findCircuitConsumption(currentMonth, currentYear)).thenReturn(List.of(
                consumptionRow("1001", "Cliente A", "Plano X", 110L, 100L), // 110% — excedido
                consumptionRow("1002", "Cliente B", "Plano Y",  90L, 100L)  //  90% — próximo do limite
        ));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.nearLimitCircuits()).hasSize(1);
        assertThat(result.nearLimitCircuits().get(0).circuit()).isEqualTo("1002");
    }

    @Test
    void getDashboard_nearLimitCircuits_sortedByPercentDescending() {
        stubCircuits(3L, 3L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(callRepository.findCircuitConsumption(currentMonth, currentYear)).thenReturn(List.of(
                consumptionRow("1001", "A", "P", 50L, 100L), // 50%
                consumptionRow("1002", "B", "P", 80L, 100L), // 80%
                consumptionRow("1003", "C", "P", 70L, 100L)  // 70%
        ));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.nearLimitCircuits())
                .extracting(DashboardDTO.CircuitConsumption::circuit)
                .containsExactly("1002", "1003", "1001");
    }

    // -------------------------------------------------------------------------
    // Circuit overage stats (gráfico donut)
    // -------------------------------------------------------------------------

    @Test
    void getDashboard_circuitOverage_countsExceededAndWithinLimit() {
        stubCircuits(3L, 3L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(callRepository.findCircuitConsumption(currentMonth, currentYear)).thenReturn(List.of(
                consumptionRow("1001", "A", "P", 110L, 100L), // excedido
                consumptionRow("1002", "B", "P",  80L, 100L), // dentro do limite
                consumptionRow("1003", "C", "P",  50L, 100L)  // dentro do limite
        ));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuitOverage().exceeded()).isEqualTo(1);
        assertThat(result.circuitOverage().withinLimit()).isEqualTo(2);
    }

    @Test
    void getDashboard_circuitOverage_allWithinLimitWhenNoneExceeded() {
        stubCircuits(2L, 2L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(callRepository.findCircuitConsumption(currentMonth, currentYear)).thenReturn(List.of(
                consumptionRow("1001", "A", "P", 60L, 100L),
                consumptionRow("1002", "B", "P", 85L, 100L)
        ));

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuitOverage().exceeded()).isEqualTo(0);
        assertThat(result.circuitOverage().withinLimit()).isEqualTo(2);
    }

    @Test
    void getDashboard_circuitOverage_zeroWhenNoCircuitsWithPackage() {
        stubCircuits(0L, 0L);
        stubTrunks(0L, 0L);
        stubCalls(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(callRepository.findCircuitConsumption(currentMonth, currentYear)).thenReturn(List.of());

        DashboardDTO result = dashboardService.getDashboard();

        assertThat(result.circuitOverage().exceeded()).isEqualTo(0);
        assertThat(result.circuitOverage().withinLimit()).isEqualTo(0);
    }

    private Object[] consumptionRow(String circuit, String customer, String plan, long used, long limit) {
        return new Object[]{circuit, customer, plan, used, limit};
    }

    private void stubCircuits(long total, long online) {
        when(circuitRepository.count()).thenReturn(total);
        when(circuitRepository.countOnline()).thenReturn(online);
    }

    private void stubTrunks(long total, long registered) {
        when(trunkRepository.count()).thenReturn(total);
        when(trunkRepository.countRegistered()).thenReturn(registered);
    }

    private void stubCalls(long total, long billSeconds, long answered, long noAnswer, long busy,
                           BigDecimal currentCost, BigDecimal prevCost) {
        when(callRepository.countByPeriod(currentMonth, currentYear)).thenReturn(total);
        when(callRepository.sumBillSecondsByPeriod(currentMonth, currentYear)).thenReturn(billSeconds);
        when(callRepository.countByDispositionAndPeriod("ANSWERED", currentMonth, currentYear)).thenReturn(answered);
        when(callRepository.countByDispositionAndPeriod("NO ANSWER", currentMonth, currentYear)).thenReturn(noAnswer);
        when(callRepository.countByDispositionAndPeriod("BUSY", currentMonth, currentYear)).thenReturn(busy);
        when(callRepository.sumCostByPeriod(currentMonth, currentYear)).thenReturn(currentCost);
        when(callRepository.sumCostByPeriod(prevMonth, prevYear)).thenReturn(prevCost);
    }
}
