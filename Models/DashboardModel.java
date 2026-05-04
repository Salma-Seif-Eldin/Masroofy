package Models;

import java.awt.Color;
import java.util.Map;

public class DashboardModel {
    private final double totalAllowance;
    private final double totalSpentCycle; // New: Total for the whole month/cycle
    private final double dailySpent;      // Today only
    private final double safeDailyLimit;
    private final Map<String, Double> categoryTotals;
    private final String statusColorKey;

    public DashboardModel(double totalAllowance, double totalSpentCycle, double dailySpent, 
                          double safeDailyLimit, Map<String, Double> categoryTotals, 
                          String statusColorKey) {
        this.totalAllowance = totalAllowance;
        this.totalSpentCycle = totalSpentCycle;
        this.dailySpent = dailySpent;
        this.safeDailyLimit = safeDailyLimit;
        this.categoryTotals = categoryTotals;
        this.statusColorKey = statusColorKey;
    }

    public double getTotalAllowance() { return totalAllowance; }
    public double getDailySpent() { return dailySpent; }
    public double getSafeDailyLimit() { return safeDailyLimit; }
    
    // FIXED: Remaining budget is Allowance minus TOTAL spent in the cycle
    public double getRemainingBudget() { return totalAllowance - totalSpentCycle; }

    public String getStatusMessage() {
        return switch (statusColorKey.toLowerCase()) {
            case "red" -> "⚠️ Critical: Budget Exceeded!";
            case "orange" -> "⚠️ Warning: 80% of budget used!";
            default -> "✅ Your budget is on track";
        };
    }

    public Color getStatusColor() {
        return switch (statusColorKey.toLowerCase()) {
            case "red" -> Color.RED;
            case "orange" -> Color.ORANGE;
            default -> new Color(50, 205, 50);
        };
    }

    public double getSpendingPercentage() {
        if (totalAllowance <= 0) return 0;
        return (totalSpentCycle / totalAllowance) * 100;
    }
}