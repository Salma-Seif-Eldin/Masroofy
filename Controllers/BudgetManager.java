package Controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import Models.BudgetCycle;
import Models.DashboardModel;
import Models.Expense;
import Database.CycleDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BudgetManager {

    private BudgetCycle currentCycle;
    private List<Expense> expenses = new ArrayList<>();
    private CycleDAO cycleDAO = new CycleDAO();
    private AlertManager alertManager = new AlertManager();
    private String currentPin;

    public void setCurrentPin(String pin) {
        this.currentPin = pin;
    }

    public String getCurrentPin() {
        return currentPin;
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

    public BudgetCycle getCurrentCycle() {
        return currentCycle;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public double getRemainingBudget() {
        if (currentCycle == null) return 0;
        return currentCycle.calculateRemainingBalance(expenses);
    }

    // ======================================================================
    // NEW: Fixed daily limit based on TOTAL allowance / TOTAL days
    // This does NOT change after expenses - each day has a fixed budget
    // ======================================================================
    public double getFixedDailyLimit() {
        if (currentCycle == null) return 0.0;
        int totalDays = currentCycle.calculateTotalDays();
        if (totalDays <= 0) return 0.0;
        return currentCycle.getTotalAllowance() / totalDays;
    }

    // ======================================================================
    // NEW: Get how much the user can still spend TODAY
    // ======================================================================
    public double getTodayRemainingDailyLimit() {
        double fixedLimit = getFixedDailyLimit();
        double todaySpent = getDailySpent();
        return Math.max(0, fixedLimit - todaySpent);
    }

    // ======================================================================
    // NEW: Check if an expense can be added without exceeding limits
    // ======================================================================
    public boolean canAddExpense(double amount) {
        if (currentCycle == null) return false;
        if (amount <= 0) return false;
        
        // Check total remaining budget
        if (getRemainingBudget() < amount) {
            return false;
        }
        
        // Check today's remaining daily limit
        if (getTodayRemainingDailyLimit() < amount) {
            return false;
        }
        
        return true;
    }

    // ======================================================================
    // NEW: Get the reason why expense cannot be added
    // Returns: "budget" or "daily_limit" or null if allowed
    // ======================================================================
    public String getExpenseRejectionReason(double amount) {
        if (currentCycle == null) return "no_cycle";
        if (amount <= 0) return "invalid_amount";
        
        if (getRemainingBudget() < amount) {
            return "budget";
        }
        
        if (getTodayRemainingDailyLimit() < amount) {
            return "daily_limit";
        }
        
        return null; // Allowed
    }

    // Keep old method for backward compatibility but mark as deprecated
    @Deprecated
    public double getDailyLimit() {
        return getFixedDailyLimit();
    }

    public double getDailySpent() {
        double todayTotal = 0;
        Date today = new Date();
        for (Expense e : expenses) {
            if (isSameDay(e.getTimestamp(), today)) {
                todayTotal += e.getAmount();
            }
        }
        return todayTotal;
    }

    public double getSpentPercentage() {
        if (currentCycle == null || currentCycle.getTotalAllowance() <= 0) return 0;
        double spent = currentCycle.getTotalAllowance() - getRemainingBudget();
        return (spent / currentCycle.getTotalAllowance()) * 100;
    }

    public String getSavedPin() {
        String sql = "SELECT value FROM settings WHERE key = 'user_pin'";
        try (java.sql.Connection conn = Database.DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (Exception e) {
            System.out.println("Error fetching PIN: " + e.getMessage());
        }
        return null;
    }

    public DashboardModel getDashboardData() {
        if (currentCycle == null) return null;

        double allowance = currentCycle.getTotalAllowance();
        double spent = getDailySpent();
        double dailyLimit = getFixedDailyLimit(); // Use fixed daily limit

        String color = "Green";
        if (getSpentPercentage() >= 100) color = "Red";
        else if (getSpentPercentage() >= 80) color = "Orange";

        Map<String, Double> categoryData = getPieChartData();

        return new DashboardModel(allowance, spent, dailyLimit, categoryData, color);
    }

    public Map<String, Double> getPieChartData() {
        Map<String, Double> data = new HashMap<>();
        if (expenses == null) return data;
        for (Expense e : expenses) {
            String name = getCategoryName(e.getCategoryId());
            data.put(name, data.getOrDefault(name, 0.0) + e.getAmount());
        }
        return data;
    }

    private String getCategoryName(int id) {
        return switch (id) {
            case 1 -> "Food";
            case 2 -> "Transport";
            case 3 -> "Shopping";
            case 4 -> "Health";
            case 5 -> "Education";
            case 6 -> "Entertainment";
            default -> "Other";
        };
    }

    public void savePin(String pin) {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES ('user_pin', ?)";
        try (java.sql.Connection conn = Database.DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            pstmt.executeUpdate();
            System.out.println("✅ PIN saved successfully.");
        } catch (Exception e) {
            System.out.println("Error saving PIN: " + e.getMessage());
        }
    }

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }
}