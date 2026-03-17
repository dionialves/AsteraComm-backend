package com.dionialves.AsteraComm.dashboard;

import com.dionialves.AsteraComm.call.CallRepository;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.trunk.TrunkRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final CircuitRepository circuitRepository;
    private final TrunkRepository trunkRepository;
    private final CallRepository callRepository;

    public DashboardService(CircuitRepository circuitRepository,
                            TrunkRepository trunkRepository,
                            CallRepository callRepository) {
        this.circuitRepository = circuitRepository;
        this.trunkRepository = trunkRepository;
        this.callRepository = callRepository;
    }

    public DashboardDTO getDashboard() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        LocalDate prev = now.minusMonths(1);
        int prevMonth = prev.getMonthValue();
        int prevYear = prev.getYear();

        // Circuits
        long totalCircuits = circuitRepository.count();
        long onlineCircuits = circuitRepository.countOnline();

        // Trunks
        long totalTrunks = trunkRepository.count();
        long registeredTrunks = trunkRepository.countRegistered();

        // Calls — current month
        long totalCalls = callRepository.countByPeriod(currentMonth, currentYear);
        long totalBillSeconds = callRepository.sumBillSecondsByPeriod(currentMonth, currentYear);
        long answered = callRepository.countByDispositionAndPeriod("ANSWERED", currentMonth, currentYear);
        long noAnswer = callRepository.countByDispositionAndPeriod("NO ANSWER", currentMonth, currentYear);
        long busy = callRepository.countByDispositionAndPeriod("BUSY", currentMonth, currentYear);

        // Billing
        BigDecimal excedents = callRepository.sumCostByPeriod(currentMonth, currentYear);
        BigDecimal prevCost = callRepository.sumCostByPeriod(prevMonth, prevYear);
        BigDecimal subscriptions = circuitRepository.sumMonthlyPrices();
        if (subscriptions == null) subscriptions = BigDecimal.ZERO;
        BigDecimal currentMonthCost = subscriptions.add(excedents);

        // Daily call stats — last 30 days
        LocalDate thirtyDaysAgo = now.minusDays(29);
        List<Object[]> dailyRaw = callRepository.findDailyCallStats(thirtyDaysAgo);
        List<DashboardDTO.DailyCallStat> dailyCalls = buildDailyCallStats(dailyRaw, thirtyDaysAgo, now);

        // Monthly billing — last 12 months
        LocalDate twelveMonthsAgo = now.minusMonths(11).withDayOfMonth(1);
        List<Object[]> monthlyRaw = callRepository.findMonthlyCallCosts(twelveMonthsAgo);
        List<DashboardDTO.MonthlyBillingStat> monthlyBilling = buildMonthlyBilling(monthlyRaw, subscriptions, now);

        // Circuit consumption — current month
        List<Object[]> consumptionRaw = callRepository.findCircuitConsumption(currentMonth, currentYear);
        List<DashboardDTO.CircuitConsumption> allConsumption = consumptionRaw.stream()
                .map(this::toCircuitConsumption)
                .collect(Collectors.toList());

        long exceeded = allConsumption.stream().filter(c -> c.percent() >= 100.0).count();
        DashboardDTO.CircuitOverageStats circuitOverage =
                new DashboardDTO.CircuitOverageStats(exceeded, allConsumption.size() - exceeded);

        List<DashboardDTO.CircuitConsumption> nearLimitCircuits = allConsumption.stream()
                .filter(c -> c.percent() < 100.0)
                .sorted(Comparator.comparingDouble(DashboardDTO.CircuitConsumption::percent).reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<DashboardDTO.TopCircuit> topCircuits = consumptionRaw.stream()
                .map(row -> new DashboardDTO.TopCircuit(
                        (String) row[1],
                        (String) row[0],
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()))
                .limit(10)
                .collect(Collectors.toList());

        return new DashboardDTO(
                new DashboardDTO.CircuitStats(totalCircuits, onlineCircuits, totalCircuits - onlineCircuits),
                new DashboardDTO.TrunkStats(totalTrunks, registeredTrunks, totalTrunks - registeredTrunks),
                new DashboardDTO.CallStats(totalCalls, totalBillSeconds / 60, answered, noAnswer, busy),
                new DashboardDTO.BillingStats(currentMonthCost, prevCost, subscriptions, excedents),
                dailyCalls,
                monthlyBilling,
                nearLimitCircuits,
                topCircuits,
                circuitOverage
        );
    }

    private List<DashboardDTO.DailyCallStat> buildDailyCallStats(List<Object[]> raw, LocalDate start, LocalDate end) {
        Map<String, long[]> map = new HashMap<>();
        for (Object[] row : raw) {
            String date = row[0].toString();
            long ans   = ((Number) row[1]).longValue();
            long noAns = ((Number) row[2]).longValue();
            long bsy   = ((Number) row[3]).longValue();
            long fail  = ((Number) row[4]).longValue();
            map.put(date, new long[]{ans, noAns, bsy, fail});
        }

        List<DashboardDTO.DailyCallStat> result = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            String key = cursor.toString();
            long[] s = map.getOrDefault(key, new long[]{0, 0, 0, 0});
            result.add(new DashboardDTO.DailyCallStat(key, s[0], s[1], s[2], s[3]));
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    private List<DashboardDTO.MonthlyBillingStat> buildMonthlyBilling(
            List<Object[]> raw, BigDecimal subscriptions, LocalDate now) {

        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : raw) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal cost = row[2] instanceof BigDecimal bd
                    ? bd
                    : BigDecimal.valueOf(((Number) row[2]).doubleValue());
            map.put(String.format("%04d-%02d", year, month), cost);
        }

        String[] monthLabels = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        List<DashboardDTO.MonthlyBillingStat> result = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String key = String.format("%04d-%02d", d.getYear(), d.getMonthValue());
            String label = monthLabels[d.getMonthValue() - 1] + "/" + String.format("%02d", d.getYear() % 100);
            BigDecimal excedents = map.getOrDefault(key, BigDecimal.ZERO);
            result.add(new DashboardDTO.MonthlyBillingStat(label, subscriptions, excedents));
        }
        return result;
    }

    private DashboardDTO.CircuitConsumption toCircuitConsumption(Object[] row) {
        String circuit = (String) row[0];
        String customerName = (String) row[1];
        String planName = (String) row[2];
        long usedMinutes = ((Number) row[3]).longValue();
        long limitMinutes = ((Number) row[4]).longValue();
        double percent = limitMinutes > 0 ? (usedMinutes * 100.0) / limitMinutes : 0.0;
        return new DashboardDTO.CircuitConsumption(circuit, customerName, planName, usedMinutes, limitMinutes, percent);
    }
}
