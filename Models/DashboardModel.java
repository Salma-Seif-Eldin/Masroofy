package Models;
import java.util.Map;

public class DashboardModel {
    private double totalAllowance;
    private double totalSpent;
    private double safeDailyLimit;
    private double remainingBudget;
    private Map<String, Double> categoryTotals; // For the Pie Chart
    private String statusColor; // "Green", "Orange", or "Red" for alerts

    // Constructor to initialize all dashboard data at once
    public DashboardModel(double totalAllowance, double totalSpent, 
                          double safeDailyLimit, Map<String, Double> categoryTotals, 
                          String statusColor) {
        this.totalAllowance = totalAllowance;
        this.totalSpent = totalSpent;
        this.safeDailyLimit = safeDailyLimit;
        this.remainingBudget = totalAllowance - totalSpent;
        this.categoryTotals = categoryTotals;
        this.statusColor = statusColor;
    }

    // Getters used by DashboardActivity to update the UI
    public double getTotalAllowance() { return totalAllowance; }
    public double getTotalSpent() { return totalSpent; }
    public double getSafeDailyLimit() { return safeDailyLimit; }
    public double getRemainingBudget() { return remainingBudget; }
    public Map<String, Double> getCategoryTotals() { return categoryTotals; }
    public String getStatusColor() { return statusColor; }
    
    // Helper to calculate percentage for the Pie Chart labels
    public double getSpendingPercentage() {
        if (totalAllowance <= 0) return 0;
        return (totalSpent / totalAllowance) * 100;
    }
}
