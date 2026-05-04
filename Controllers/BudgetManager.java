package Controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import Models.BudgetCycle;
import Models.DashboardModel;
import Models.Expense;
import Models.Category;
import Database.CycleDAO;
import Database.UserDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BudgetManager {

    private BudgetCycle currentCycle;
    private List<Expense> expenses = new ArrayList<>();
    private CycleDAO cycleDAO = new CycleDAO();
    private UserDAO userDAO = new UserDAO();
    private AlertManager alertManager = new AlertManager();
    private String currentPin;

    public void setCurrentPin(String pin) {
        this.currentPin = pin;
    }

    public String getCurrentPin() {
        return currentPin;
    }
    public double getTotalSpent() {
        if (currentCycle == null) return 0;
        return currentCycle.getTotalAllowance() - getRemainingBudget();
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public boolean registerPin(String pin) {
        return userDAO.registerPin(pin);
    }

    public boolean pinExists(String pin) {
        return userDAO.pinExists(pin);
    }

    public void savePin(String pin) {
        registerPin(pin);
    }

    public String getSavedPin() {
        return userDAO.getFirstPin();
    }

    // Inside BudgetManager.java
public void loadExistingBudget() {
    if (currentPin == null) return;
    currentCycle = cycleDAO.getLastSavedCycle(currentPin);
    if (currentCycle != null) {
        expenses = cycleDAO.getExpensesByCycle(currentCycle.getCycleId(), currentPin);
        
        currentCycle.calculateRemainingBalance(expenses); 
        
        System.out.println("Loaded " + expenses.size() + " expenses.");
    }
}

public DashboardModel getDashboardData() {
    if (currentCycle == null) return null;

    double allowance = currentCycle.getTotalAllowance();
    
    // 1. Calculate Total Spent in the entire cycle
    double totalSpentCycle = 0;
    for (Expense e : expenses) {
        totalSpentCycle += e.getAmount();
    }

    // 2. Calculate Today's Spent only
    double dailySpent = getDailySpent();
    
    double dailyLimit = getFixedDailyLimit();

    String color = "Green";
    double pct = (totalSpentCycle / allowance) * 100;
    if (pct >= 100) color = "Red";
    else if (pct >= 80) color = "Orange";

    return new DashboardModel(allowance, totalSpentCycle, dailySpent, dailyLimit, getPieChartData(), color);
}

    // Inside BudgetManager.java



public String startCycle(double allowance, Date start, Date end) {
    if (currentPin == null) return "no_user";

    if (allowance <= 0) {
        return "invalid_allowance";
    }

    if (start == null || end == null || end.before(start)) {
        return "invalid_dates";
    }

    BudgetCycle newCycle = new BudgetCycle();
    newCycle.setUserPin(currentPin);
    newCycle.setTotalAllowance(allowance);
    newCycle.setStartDate(start);
    newCycle.setEndDate(end);

    boolean saved = cycleDAO.saveNewCycle(newCycle, currentPin);
    if (saved) {
        this.currentCycle = newCycle;
        this.expenses.clear();
        return "success";
    }
    
    return "db_error";
}

    public BudgetCycle getCurrentCycle() { return currentCycle; }
    public List<Expense> getExpenses()   { return expenses; }

    public double getRemainingBudget() {
        if (currentCycle == null) return 0;
        return currentCycle.calculateRemainingBalance(expenses);
    }

    public double getFixedDailyLimit() {
        if (currentCycle == null) return 0.0;
        int totalDays = currentCycle.calculateTotalDays();
        if (totalDays <= 0) return 0.0;
        return currentCycle.getTotalAllowance() / totalDays;
    }

    public double getTodayRemainingDailyLimit() {
        double fixedLimit = getFixedDailyLimit();
        double todaySpent = getDailySpent();
        return Math.max(0, fixedLimit - todaySpent);
    }

    public boolean canAddExpense(double amount) {
        return getExpenseRejectionReason(amount) == null;
    }



public String getExpenseRejectionReason(double amount) {
    if (currentCycle == null) return "no_cycle";
    
    double totalRemaining = currentCycle.getTotalAllowance() - getTotalSpent();

    if (amount > totalRemaining) {
        return "total_budget_exhausted";
    }

    return null; 
}

    @Deprecated
    public double getDailyLimit() { return getFixedDailyLimit(); }

    public double getDailySpent() {
        double todayTotal = 0;
        Date today = new Date();
        for (Expense e : expenses) {
            if (isSameDay(e.getTimestamp(), today)) todayTotal += e.getAmount();
        }
        return todayTotal;
    }

    public double getSpentPercentage() {
        if (currentCycle == null || currentCycle.getTotalAllowance() <= 0) return 0;
        double spent = currentCycle.getTotalAllowance() - getRemainingBudget();
        return (spent / currentCycle.getTotalAllowance()) * 100;
    }

    


    public Map<String, Double> getPieChartData() {
        Map<String, Double> data = new HashMap<>();
        if (expenses == null) return data;
        for (Expense e : expenses) {
            String name = Category.getNameById(e.getCategoryId());
            data.put(name, data.getOrDefault(name, 0.0) + e.getAmount());
        }
        return data;
    }

    public long calculateDaysRemaining(Date endDate) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate end = LocalDate.parse(
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(endDate));
            long days = ChronoUnit.DAYS.between(today, end) + 1;
            return (days <= 0) ? 1 : days;
        } catch (Exception e) {
            return 1;
        }
    }


private boolean isSameDay(Date d1, Date d2) {
    if (d1 == null || d2 == null) return false;
    
    java.time.LocalDate date1 = new java.sql.Date(d1.getTime()).toLocalDate();
    java.time.LocalDate date2 = new java.sql.Date(d2.getTime()).toLocalDate();
    
    return date1.equals(date2);
}
    // Inside BudgetManager.java

public void refreshBudgetState() {
    if (currentCycle != null) {
        // Recalculate the balance using the current list of expenses
        currentCycle.calculateRemainingBalance(this.expenses);
    }
}

// Update your add method (or wherever expenses are added)
public void addExpenseLocal(Expense e) {
    this.expenses.add(e);
    refreshBudgetState(); // <--- CRITICAL
}
public void addExpense(Expense exp) {
    this.expenses.add(exp);
    
    // Refresh calculations
    double totalSpent = getTotalSpent();
    double totalAllowance = currentCycle.getTotalAllowance();
    double todaySpent = getDailySpent();
    double dailyLimit = getFixedDailyLimit();

    // Trigger Monitoring for 80% and 100% thresholds
    alertManager.monitorBudgetTotalSpent(totalSpent, totalAllowance);
    alertManager.monitorDailyLimit(todaySpent, dailyLimit);
}

}


