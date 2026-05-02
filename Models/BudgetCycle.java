package Models;

import java.util.Date;
import java.util.List;

public class BudgetCycle {

    private int cycleId;
    private String userPin;
    private double totalAllowance;
    private Date startDate;
    private Date endDate;
    private double remainingBalance;
    private double safeDailyLimit;

    public int getCycleId() { return cycleId; }
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    public String getUserPin() { return userPin; }
    public void setUserPin(String userPin) { this.userPin = userPin; }

    public double getTotalAllowance() { return totalAllowance; }
    public void setTotalAllowance(double totalAllowance) { 
        this.totalAllowance = totalAllowance; 
    }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public double getRemainingBalance() { return remainingBalance; }

    public int calculateTotalDays() {
        if (startDate != null && endDate != null) {
            long diff = endDate.getTime() - startDate.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        }
        return 0;
    }

    public double calculateDailyLimit() {
        int totalDays = calculateTotalDays();
        if (totalDays > 0) {
            this.safeDailyLimit = totalAllowance / totalDays;
            return this.safeDailyLimit;
        }
        return 0;
    }

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

    public double getSpentPercentage() {
        if (totalAllowance <= 0) return 0.0;
        double spent = totalAllowance - remainingBalance;
        return (spent / totalAllowance) * 100;
    }

    public boolean isEightyPercentReached() {
        return getSpentPercentage() >= 80.0;
    }

    public void applyRollover() {
        this.totalAllowance = this.totalAllowance + this.remainingBalance;
        calculateDailyLimit();
    }

    public void resetCycle() {
        this.totalAllowance = 0;
        this.remainingBalance = 0;
        this.safeDailyLimit = 0;
    }
}