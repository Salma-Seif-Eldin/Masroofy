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

    // =========================================================================
    // MULTI-USER: Register a new PIN into the 'users' table
    // Returns false if that PIN is already taken
    // =========================================================================
    public boolean registerPin(String pin) {
        // Check if already exists
        if (pinExists(pin)) return false;

        String sql = "INSERT INTO users (pin) VALUES (?)";
        try (java.sql.Connection conn = Database.DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            pstmt.executeUpdate();
            System.out.println("✅ PIN registered: " + pin);
            return true;
        } catch (Exception e) {
            System.out.println("Error registering PIN: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // MULTI-USER: Check if a PIN exists in the 'users' table
    // =========================================================================
    public boolean pinExists(String pin) {
        String sql = "SELECT pin FROM users WHERE pin = ?";
        try (java.sql.Connection conn = Database.DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            java.sql.ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Error checking PIN: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // Kept for backward-compat — now delegates to registerPin()
    // =========================================================================
    public void savePin(String pin) {
        registerPin(pin);
    }

    // =========================================================================
    // Kept for backward-compat — returns ONE saved pin (first found).
    // AuthActivity no longer uses this; left so nothing breaks.
    // =========================================================================
    public String getSavedPin() {
        String sql = "SELECT pin FROM users LIMIT 1";
        try (java.sql.Connection conn = Database.DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("pin");
        } catch (Exception e) {
            System.out.println("Error fetching PIN: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // Load budget data for whoever is logged in (currentPin)
    // =========================================================================
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

    // Fixed daily limit: allowance / total days — does not change after expenses
    public double getFixedDailyLimit() {
        if (currentCycle == null) return 0.0;
        int totalDays = currentCycle.calculateTotalDays();
        if (totalDays <= 0) return 0.0;
        return currentCycle.getTotalAllowance() / totalDays;
    }

    // How much the user can still spend TODAY
    public double getTodayRemainingDailyLimit() {
        double fixedLimit = getFixedDailyLimit();
        double todaySpent = getDailySpent();
        return Math.max(0, fixedLimit - todaySpent);
    }

    // Check if an expense can be added without exceeding any limit
    public boolean canAddExpense(double amount) {
        return getExpenseRejectionReason(amount) == null;
    }

    // Returns the rejection reason string, or null if the expense is allowed
    public String getExpenseRejectionReason(double amount) {
        if (currentCycle == null) return "no_cycle";
        if (amount <= 0)          return "invalid_amount";
        if (getRemainingBudget()          < amount) return "budget";
        if (getTodayRemainingDailyLimit() < amount) return "daily_limit";
        return null;
    }

    /** @deprecated use getFixedDailyLimit() */
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
            String name = getCategoryName(e.getCategoryId());
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

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }
}
