package Models;

import java.util.Date;
import java.util.List;

public class BudgetCycle {

    private int cycleId;
    private double totalAllowance;
    private Date startDate;
    private Date endDate;
    private double remainingBalance;
    private double safeDailyLimit;

//Setters and getters

    public int getCycleId() { return cycleId; }
    // Returns the cycle's database ID
    // Used by CycleDAO to link expenses to this cycle

    public void setCycleId(int cycleId) { this.cycleId = cycleId; }
    // Sets the ID after database saves it
    // "this.cycleId" = the field, "cycleId" = the parameter

    public double getTotalAllowance() { return totalAllowance; }
    // Used by BudgetManager to calculate percentages

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    // Used by CycleDAO when saving/loading cycle dates

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    // Used by CycleDAO when saving/loading cycle dates

    public double getSafeDailyLimit() { return safeDailyLimit; }
    // Used by Dashboard to display daily limit to user

    public double getRemainingBalance() { return remainingBalance; }

    // ─────────────────────────────────────────
    // CORE LOGIC
    // ─────────────────────────────────────────

    // Main logic 


    // validity check for the date
    public boolean isValidDates(Date start, Date end) {
        if (start == null || end == null) return false; 
        return end.after(start);
    }

    public boolean initialize(double allowance, Date start, Date end)// called when a user cretes a new cycle
     {
        if (allowance <= 0) return false; //  allowance validation
        if (isValidDates(start, end)) {
            this.totalAllowance = allowance;
            this.startDate = start;
            this.endDate = end;
            this.remainingBalance = allowance;
            this.safeDailyLimit = calculateDailyLimit();
            return true;
        }
        return false;// then date is not vaild
    }

    public int calculateTotalDays() {// to count days in the cycle 
        if (startDate != null && endDate != null) {
            long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
            return (int) (diffInMillies / (1000 * 60 * 60 * 24));
        }
        return 0;
    }

    public double calculateDailyLimit() {
        int totalDays = calculateTotalDays();
        if (totalDays > 0) {
            return totalAllowance / totalDays; // daily limit calc 
        }
        return 0;
    }

    public double calculateRemainingBalance(List<Expense> expenses) {
        double totalSpent = 0.0;
        if (expenses != null) { 
            for (Expense e : expenses) {
                totalSpent += e.getAmount();// add each expense to the total spent 
            }
        }
        this.remainingBalance = totalAllowance - totalSpent; //update the remaning after the loop ends 
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

   //add the leftovers of the past cylce to the neext cycle 
    public void applyRollover() {
        this.totalAllowance = this.totalAllowance + this.remainingBalance;
        this.safeDailyLimit = calculateDailyLimit();
    }

   // deletes  all old dataa to start a new cycle
    public void resetCycle() {
        this.totalAllowance = 0;
        this.remainingBalance = 0;
        this.safeDailyLimit = 0;
        this.startDate = null;
        this.endDate = null;
    }
}