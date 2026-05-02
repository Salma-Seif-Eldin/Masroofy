package Controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
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
    public long calculateDaysRemaining(Date endDateStr) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate end = LocalDate.parse(new java.text.SimpleDateFormat("yyyy-MM-dd").format(endDateStr)); // تأكدي من Format التاريخ yyyy-MM-dd
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

public double getDailyLimit() {
    if (currentCycle == null) return 0.0;
    long daysRemaining = calculateDaysRemaining(currentCycle.getEndDate());
    if (daysRemaining <= 0) daysRemaining = 1;
    return getRemainingBudget() / daysRemaining;
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
        double spent = getDailySpent(); // أو المجموع الكلي حسب منطقك
        double dailyLimit = getDailyLimit();
        
        // تحديد اللون بناءً على النسبة
        String color = "Green";
        if (getSpentPercentage() >= 90) color = "Red";
        else if (getSpentPercentage() >= 80) color = "Orange";

        // إرجاع كائن الموديل للـ View
        return new DashboardModel(allowance, spent, dailyLimit, null, color);
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