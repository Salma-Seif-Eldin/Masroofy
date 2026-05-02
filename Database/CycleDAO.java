package Database;

import Models.BudgetCycle;
import Models.Expense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CycleDAO {
    public List<Expense> getExpensesByCycle(int cycleId, String userPin) {
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
                expenses.add(e);
            }
        } catch (Exception e) {
            System.out.println("Error loading expenses for user " + userPin + ": " + e.getMessage());
        }
        return expenses;
    }

    public boolean saveNewCycle(BudgetCycle cycle, String userPin) {
        String sql = "INSERT INTO budget_cycles(user_pin, total_allowance, start_date, end_date) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userPin);
            pstmt.setDouble(2, cycle.getTotalAllowance());
            pstmt.setString(3, cycle.getStartDate().toString());
            pstmt.setString(4, cycle.getEndDate().toString());
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
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userPin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BudgetCycle cycle = new BudgetCycle();
                cycle.setCycleId(rs.getInt("cycle_id"));
                cycle.setUserPin(rs.getString("user_pin"));
                cycle.setTotalAllowance(rs.getDouble("total_allowance"));
                return cycle;
            }
        } catch (Exception e) {
            System.out.println("Error fetching last cycle: " + e.getMessage());
        }
        return null;
    }
}