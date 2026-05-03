package Models;

import java.awt.Color;
import java.util.Map;

public class DashboardModel {
    private double totalAllowance;
    private double totalSpent;
    private double safeDailyLimit;
    private double remainingBudget;
    private Map<String, Double> categoryTotals;
    private String statusColorKey;

    public DashboardModel(double totalAllowance, double totalSpent, 
                          double safeDailyLimit, Map<String, Double> categoryTotals, 
                          String statusColorKey) {
        this.totalAllowance = totalAllowance;
        this.totalSpent = totalSpent;
        this.safeDailyLimit = safeDailyLimit;
        this.remainingBudget = totalAllowance - totalSpent;
        this.categoryTotals = categoryTotals;
        this.statusColorKey = statusColorKey;
    }

    public double getTotalAllowance() { return totalAllowance; }
    public double getTotalSpent() { return totalSpent; }
    public double getSafeDailyLimit() { return safeDailyLimit; }
    public double getRemainingBudget() { return remainingBudget; }
    public Map<String, Double> getCategoryTotals() { return categoryTotals; }
    public String getStatusColorKey() { return statusColorKey; }

    // NEW: Model decides the message and color
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
        return (totalSpent / totalAllowance) * 100;
    }
}
