package Models;

import java.awt.Color;
import java.util.Map;

/**
 * Immutable model representing the data displayed on the Dashboard screen.
 * <p>
 * Encapsulates budget summary information including the total allowance,
 * cycle spending, daily spending, category breakdowns, and the current
 * budget status. Used by {@code DashboardActivity} to update the UI.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class DashboardModel {
    private final double totalAllowance;
    private final double totalSpentCycle;
    private final double dailySpent;
    private final double safeDailyLimit;
    private final Map<String, Double> categoryTotals;
    private final String statusColorKey;

    /**
     * Constructs a {@code DashboardModel} with all required dashboard data.
     *
     * @param totalAllowance  the total budget allowance for the current cycle
     * @param totalSpentCycle the total amount spent during the entire cycle
     * @param dailySpent      the amount spent today only
     * @param safeDailyLimit  the recommended daily spending limit
     * @param categoryTotals  a map of category names to their total spending amounts
     * @param statusColorKey  a string key indicating budget status: "green", "orange", or "red"
     */
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

    /**
     * Returns the total budget allowance for the current cycle.
     *
     * @return the total allowance amount
     */
    public double getTotalAllowance() { return totalAllowance; }

    /**
     * Returns the amount spent today only (not the full cycle).
     *
     * @return today's spending amount
     */
    public double getDailySpent() { return dailySpent; }

    /**
     * Returns the recommended safe daily spending limit.
     *
     * @return the daily limit amount
     */
    public double getSafeDailyLimit() { return safeDailyLimit; }

    /**
     * Returns the remaining budget for the current cycle.
     * <p>
     * Calculated as total allowance minus total spent in the cycle.
     * </p>
     *
     * @return the remaining budget amount
     */
    public double getRemainingBudget() { return totalAllowance - totalSpentCycle; }

    /**
     * Returns a human-readable status message based on the current budget status.
     *
     * @return a status message string (e.g., warning or on-track message)
     */
    public String getStatusMessage() {
        return switch (statusColorKey.toLowerCase()) {
            case "red"    -> "⚠️ Critical: Budget Exceeded!";
            case "orange" -> "⚠️ Warning: 80% of budget used!";
            default       -> "✅ Your budget is on track";
        };
    }

    /**
     * Returns the color associated with the current budget status.
     *
     * @return {@link Color#RED} if exceeded, {@link Color#ORANGE} if near limit,
     *         or green if on track
     */
    public Color getStatusColor() {
        return switch (statusColorKey.toLowerCase()) {
            case "red"    -> Color.RED;
            case "orange" -> Color.ORANGE;
            default       -> new Color(50, 205, 50);
        };
    }

    /**
     * Calculates the percentage of the total allowance that has been spent in the cycle.
     *
     * @return the spending percentage (0–100), or {@code 0} if allowance is not set
     */
    public double getSpendingPercentage() {
        if (totalAllowance <= 0) return 0;
        return (totalSpentCycle / totalAllowance) * 100;
    }
}