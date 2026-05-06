package Models;

import java.util.Date;
import java.util.List;

/**
 * Represents a single budget cycle for a user.
 * <p>
 * A budget cycle defines a period (start date to end date) during which
 * the user tracks their spending against a total allowance. It provides
 * methods for calculating daily limits, remaining balances, and spending
 * percentages.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class BudgetCycle {

    /**
     * Creates an empty budget cycle instance.
     */
    public BudgetCycle() {
    }

    private int cycleId;
    private String userPin;
    private double totalAllowance;
    private Date startDate;
    private Date endDate;
    private double remainingBalance;
    private double safeDailyLimit;

    /**
     * Returns the unique identifier of this budget cycle.
     *
     * @return the cycle ID
     */
    public int getCycleId() {
        return cycleId;
    }

    /**
     * Sets the unique identifier of this budget cycle.
     *
     * @param cycleId the cycle ID to set
     */
    public void setCycleId(int cycleId) {
        this.cycleId = cycleId;
    }

    /**
     * Returns the PIN of the user who owns this cycle.
     *
     * @return the user's PIN string
     */
    public String getUserPin() {
        return userPin;
    }

    /**
     * Sets the PIN of the user who owns this cycle.
     *
     * @param userPin the user PIN to associate with this cycle
     */
    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }

    /**
     * Returns the total allowance (budget) for this cycle.
     *
     * @return the total allowance amount
     */
    public double getTotalAllowance() {
        return totalAllowance;
    }

    /**
     * Sets the total allowance for this cycle.
     *
     * @param totalAllowance the allowance amount (must be positive)
     */
    public void setTotalAllowance(double totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    /**
     * Returns the start date of this budget cycle.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of this budget cycle.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date of this budget cycle.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of this budget cycle.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the current remaining balance for this cycle.
     *
     * @return the remaining balance
     */
    public double getRemainingBalance() {
        return remainingBalance;
    }

    /**
     * Calculates the total number of days in this budget cycle (inclusive).
     *
     * @return the number of days between start and end date, or {@code 0} if dates
     *         are not set
     */
    public int calculateTotalDays() {
        if (startDate != null && endDate != null) {
            long diff = endDate.getTime() - startDate.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        }
        return 0;
    }

    /**
     * Calculates the safe daily spending limit based on the total allowance and
     * cycle duration.
     * <p>
     * Also updates the internal {@code safeDailyLimit} field.
     * </p>
     *
     * @return the daily limit amount, or {@code 0} if the cycle has no valid days
     */
    public double calculateDailyLimit() {
        int totalDays = calculateTotalDays();
        if (totalDays > 0) {
            this.safeDailyLimit = totalAllowance / totalDays;
            return this.safeDailyLimit;
        }
        return 0;
    }

    /**
     * Calculates the remaining balance after summing all provided expenses.
     * <p>
     * Also updates the internal {@code remainingBalance} field.
     * </p>
     *
     * @param expenses the list of expenses to deduct from the allowance; may be
     *                 {@code null}
     * @return the remaining balance after all expenses are deducted
     */
    public double calculateRemainingBalance(List<Expense> expenses) {
        double totalSpent = 0.0;
        if (expenses != null) {
            for (Expense e : expenses) {
                totalSpent += e.getAmount();
            }
        }
        this.remainingBalance = totalAllowance - totalSpent;
        return this.remainingBalance;
    }

    /**
     * Calculates the percentage of the total allowance that has been spent.
     *
     * @return the spent percentage (0–100), or {@code 0.0} if allowance is not set
     */
    public double getSpentPercentage() {
        if (totalAllowance <= 0)
            return 0.0;
        double spent = totalAllowance - remainingBalance;
        return (spent / totalAllowance) * 100;
    }

    /**
     * Checks whether 80% or more of the total allowance has been spent.
     *
     * @return {@code true} if spending has reached or exceeded 80%; {@code false}
     *         otherwise
     */
    public boolean isEightyPercentReached() {
        return getSpentPercentage() >= 80.0;
    }

    /**
     * Applies a rollover by adding the remaining balance to the total allowance
     * and recalculating the daily limit.
     */
    public void applyRollover() {
        this.totalAllowance = this.totalAllowance + this.remainingBalance;
        calculateDailyLimit();
    }

    /**
     * Resets the cycle by zeroing out the allowance, remaining balance, and daily
     * limit.
     */
    public void resetCycle() {
        this.totalAllowance = 0;
        this.remainingBalance = 0;
        this.safeDailyLimit = 0;
    }
}