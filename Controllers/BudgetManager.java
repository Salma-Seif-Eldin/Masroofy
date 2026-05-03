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
    private UserDAO userDAO = new UserDAO();  // NEW: use DAO instead of raw SQL
    private AlertManager alertManager = new AlertManager();
    private String currentPin;

    public void setCurrentPin(String pin) {
        this.currentPin = pin;
    }

    public String getCurrentPin() {
        return currentPin;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    // FIXED: Delegate to UserDAO
    public boolean registerPin(String pin) {
        return userDAO.registerPin(pin);
    }

    // FIXED: Delegate to UserDAO
    public boolean pinExists(String pin) {
        return userDAO.pinExists(pin);
    }

    public void savePin(String pin) {
        registerPin(pin);
    }

    // FIXED: Delegate to UserDAO
    public String getSavedPin() {
        return userDAO.getFirstPin();
    }

    public void loadExistingBudget() {
        if (currentPin == null) return;

        currentCycle = cycleDAO.getLastSavedCycle(currentPin);

        if (currentCycle != null) {
            expenses = cycleDAO.getExpensesByCycle(currentCycle.getCycleId(), currentPin);
            currentCycle.calculateRemainingBalance(expenses);
            currentCycle.calculateDailyLimit();
            alertManager.requestPermissions();
            System.out.println("✅ Data loaded for user: " + currentPin);
        } else {
            expenses = new ArrayList<>();
            System.out.println("ℹ️ No existing budget for user: " + currentPin);
        }
    }

    public boolean startCycle(double allowance, Date start, Date end) {
        if (currentPin == null) return false;

        BudgetCycle newCycle = new BudgetCycle();
        newCycle.setUserPin(currentPin);
        newCycle.setTotalAllowance(allowance);
        newCycle.setStartDate(start);
        newCycle.setEndDate(end);

        boolean saved = cycleDAO.saveNewCycle(newCycle, currentPin);
        if (saved) {
            this.currentCycle = newCycle;
            this.expenses.clear();
            return true;
        }
        return false;
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
        if (amount <= 0)          return "invalid_amount";
        if (getRemainingBudget()          < amount) return "budget";
        if (getTodayRemainingDailyLimit() < amount) return "daily_limit";
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

    public DashboardModel getDashboardData() {
        if (currentCycle == null) return null;
        double allowance  = currentCycle.getTotalAllowance();
        double spent      = getDailySpent();
        double dailyLimit = getFixedDailyLimit();

        String color = "Green";
        if (getSpentPercentage() >= 100) color = "Red";
        else if (getSpentPercentage() >= 80) color = "Orange";

        return new DashboardModel(allowance, spent, dailyLimit, getPieChartData(), color);
    }

    public Map<String, Double> getPieChartData() {
        Map<String, Double> data = new HashMap<>();
        if (expenses == null) return data;
        for (Expense e : expenses) {
            // FIXED: Use Category model
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

    // REMOVED: getCategoryName() - now uses Category.getNameById()

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }
}
