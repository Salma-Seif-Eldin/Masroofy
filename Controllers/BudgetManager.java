package Controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;// for pie chart
import Models.BudgetCycle;
import Models.Expense;
import Database.CycleDAO;


public class BudgetManager {

    private BudgetCycle currentCycle;
    private List<Expense> expenses = new ArrayList<>();//using alist avoids hitting the database on every calculation 
    private CycleDAO cycleDAO = new CycleDAO(); //Database handler 
    private AlertManager alertManager = new AlertManager();//handels warnings


    public BudgetCycle getCurrentCycle() {
    return currentCycle;
}

    public void loadExistingBudget() {
        //checks if their is a saved cycle
        currentCycle = cycleDAO.getLastSavedCycle();
        // to get most recent cycle
        if (currentCycle != null) {
            expenses = cycleDAO.getExpensesByCycle(currentCycle.getCycleId());
             // load all expenses that belong to this cycle
            alertManager.requestPermissions();
             // make sure notifications are enabled
            System.out.println("Welcome back! Loaded saved budget: " + currentCycle.getTotalAllowance());
        } else {
            System.out.println("No existing budget found. Please start a new cycle.");
        }
    }

 //cycle methods

    public boolean startCycle(double allowance, Date start, Date end) {
        currentCycle = new BudgetCycle();
        //creates new budget cycle 
        boolean success = currentCycle.initialize(allowance, start, end);
       //checks if inputs are valid 

        if (success) {
            cycleDAO.saveNewCycle(currentCycle);
            expenses = new ArrayList<>(); // reset expenses for new cycle
            alertManager.requestPermissions();
            return true;
        }
        return false;
        // Validation failed — View should show error message
    }

    public double getDailyLimit() {
        return (currentCycle != null) ? currentCycle.calculateDailyLimit() : 0.0;
         // if cycle exists return limit, else 0
         // Used by Dashboard to show "Safe to spend today: X EGP"
    }

    public double getRemainingBudget() {
        if (currentCycle == null) return 0.0;
        return currentCycle.calculateRemainingBalance(expenses);
         // Passes expense list to BudgetCycle which does the math
        // = totalAllowance - sum of all expense amounts
    }

    //resets 
    public void resetCycle() {
        currentCycle = null;
        expenses.clear();
        alertManager.clearNotifications();
        System.out.println("Cycle has been reset.");
    }

      // BudgetCycle adds remaining balance to allowance
    public void applyRollover() {
        if (currentCycle != null) {
            currentCycle.applyRollover();
            cycleDAO.saveNewCycle(currentCycle);
            System.out.println("Rollover applied. New allowance: " + currentCycle.getTotalAllowance());
        }
    }

    //Expense method

    
    public boolean addExpense(double amount, int categoryId, String notes) {
        if (currentCycle == null) {
            System.out.println("No active cycle. Please start a budget cycle first.");
            return false;
             // Can't add expense without an active budget
        }

        Expense expense = new Expense(amount, categoryId, notes);
         //Create expense obj and set timestamp to now 

        if (!expense.save()) {
            System.out.println("Expense validation failed.");
            return false;
        }

        // link expense to current cycle
        expense.setCycleId(currentCycle.getCycleId());
        boolean saved = cycleDAO.insertExpense(expense);

        if (saved) {
            expenses.add(expense);

            // Use AlertManager to monitor thresholds
            double totalSpent = currentCycle.getTotalAllowance() - getRemainingBudget();
            alertManager.monitorBudgetTotalSpent( totalSpent,currentCycle.getTotalAllowance()
            );
            alertManager.monitorDailyLimit(getDailySpent(),getDailyLimit());

            System.out.println("Expense added. New balance: " + getRemainingBudget());
            return true;
        }
        return false;
    }

  
    public boolean editExpense(int id, double amount, int categoryId) {
        for (Expense e : expenses) {
            if (e.getExpenseId() == id) {
                // Found the right expense in memory
                boolean edited = e.editExpense(amount, categoryId, e.getNotes());
                // Expense validates and updates its own fields
                if (edited) {
                    return cycleDAO.updateExpense(e);
                     // Sync the change to the database
                }
            }
        }
        System.out.println("Expense not found with ID: " + id);
        return false;
        // Expense with that ID not found
    }


    public boolean deleteExpense(int id) {
        boolean deleted = cycleDAO.deleteExpense(id);
        // Remove from database first
        if (deleted) {
            expenses.removeIf(e -> e.getExpenseId() == id);
            System.out.println("Expense deleted: " + id);
        }
        return deleted;
    }

   
    public List<Expense> getExpenses() {
        return expenses;
        //used by history to return expense list 
    }

   // calculations methods

    public double getSpentPercentage() {
        if (currentCycle == null || currentCycle.getTotalAllowance() <= 0) return 0.0;
        double allowance = currentCycle.getTotalAllowance();
        double spent = allowance - getRemainingBudget();
        return (spent / allowance) * 100;
    }

  
    public double getDailySpent() {
        if (expenses == null || expenses.isEmpty()) return 0.0;

        double todaySpent = 0.0;
        Date today = new Date();

        for (Expense e : expenses) {
            // Compare year/month/day only
            if (isSameDay(e.getTimestamp(), today)) {
                todaySpent += e.getAmount();
            }
        }
        return todaySpent;
    }
    public double recalculateLimits() {
        // Called after any edit/delete to past expenses
        // Forces a full recalculation of remaining budget and daily limit
        // This is necessary because changing past data can affect current/future limits
        return getRemainingBudget();
    }

    
    public Map<String, Double> getPieChartData() {
        //returns spend groubed by category 
        Map<String, Double> chartData = new HashMap<>();

        if (expenses == null || expenses.isEmpty()) {
            System.out.println("No expenses to chart.");
            return chartData;
        }

        for (Expense e : expenses) {
            String categoryName = getCategoryName(e.getCategoryId());

            // If category exists, add to it — if not, start from 0
            chartData.put( categoryName,chartData.getOrDefault(categoryName, 0.0) + e.getAmount()
            );
        }

        return chartData;
    }

  // secuirty methods
    public boolean verifyFingerprint() {
        // Will connect to PrivacyLock / biometric system
        System.out.println("Fingerprint verification requested.");
        return false;
    }

  //private helper methods 

    // Converts categoryId integer to readable name for pie chart
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1:  return "Food";
            case 2:  return "Transport";
            case 3:  return "Shopping";
            case 4:  return "Health";
            case 5:  return "Education";
            case 6:  return "Entertainment";
            default: return "Other";
        }
    }

   // Compares year, month, day — ignores hours/minutes/seconds
    @SuppressWarnings("deprecation")
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return d1.getYear()  == d2.getYear()
            && d1.getMonth() == d2.getMonth()
            && d1.getDate()  == d2.getDate();
    }
}