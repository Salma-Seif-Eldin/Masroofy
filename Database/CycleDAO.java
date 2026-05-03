package Database;

import Models.BudgetCycle;
import Models.Expense;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CycleDAO {

    public List<Expense> getExpensesByCycle(int cycleId, String userPin) {
        // FIX: expenses table has user_pin column, query must use it
        String sql = "SELECT * FROM expenses WHERE cycle_id = ? AND user_pin = ?";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cycleId);
            pstmt.setString(2, userPin);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Expense e = new Expense(
                    rs.getDouble("amount"),
                    rs.getInt("category_id"),
                    rs.getString("notes")
                );
                e.setExpenseId(rs.getInt("expense_id"));
                e.setCycleId(rs.getInt("cycle_id"));
                // FIX: date is stored as TEXT "yyyy-MM-dd", parse it properly
                String dateStr = rs.getString("date");
                if (dateStr != null) {
                    try {
                        Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                        e.setTimestamp(d);
                    } catch (Exception ex) {
                        e.setTimestamp(new Date());
                    }
                } else {
                    e.setTimestamp(new Date());
                }
                expenses.add(e);
            }
        } catch (Exception e) {
            System.out.println("Error loading expenses for user " + userPin + ": " + e.getMessage());
        }
        return expenses;
    }

    public boolean saveNewCycle(BudgetCycle cycle, String userPin) {
        String sql = "INSERT INTO budget_cycles(user_pin, total_allowance, start_date, end_date) VALUES(?,?,?,?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userPin);
            pstmt.setDouble(2, cycle.getTotalAllowance());
            pstmt.setString(3, sdf.format(cycle.getStartDate()));
            pstmt.setString(4, sdf.format(cycle.getEndDate()));
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                cycle.setCycleId(keys.getInt(1));
                cycle.setUserPin(userPin);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error saving cycle: " + e.getMessage());
            return false;
        }
    }

    public BudgetCycle getLastSavedCycle(String userPin) {
        String sql = "SELECT * FROM budget_cycles WHERE user_pin = ? ORDER BY cycle_id DESC LIMIT 1";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userPin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BudgetCycle cycle = new BudgetCycle();
                cycle.setCycleId(rs.getInt("cycle_id"));
                cycle.setUserPin(rs.getString("user_pin"));
                cycle.setTotalAllowance(rs.getDouble("total_allowance"));
                // FIX: load start_date and end_date so getDailyLimit() works correctly
                String startStr = rs.getString("start_date");
                String endStr = rs.getString("end_date");
                if (startStr != null) {
                    try { cycle.setStartDate(sdf.parse(startStr)); } catch (Exception ignored) {}
                }
                if (endStr != null) {
                    try { cycle.setEndDate(sdf.parse(endStr)); } catch (Exception ignored) {}
                }
                return cycle;
            }
        } catch (Exception e) {
            System.out.println("Error fetching last cycle: " + e.getMessage());
        }
        return null;
    }
}
