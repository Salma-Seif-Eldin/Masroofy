package Database;

import Models.Expense;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {

    public boolean saveExpense(Expense expense) {
        String sql = "INSERT INTO expenses (amount, category_id, notes, date, cycle_id, user_pin) " +
                     "VALUES (?, ?, ?, Date('now'), ?, ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, expense.getAmount());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setString(3, expense.getNotes());
            pstmt.setInt(4, expense.getCycleId());
            pstmt.setString(5, expense.getUserPin());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error saving expense: " + e.getMessage());
            return false;
        }
    }

    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY date DESC";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all expenses: " + e.getMessage());
        }
        return list;
    }

    public List<Expense> getFilteredExpenses(int categoryID, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        String sql = (categoryID == 0)
            ? "SELECT * FROM expenses WHERE date >= ? AND date <= ? ORDER BY date DESC"
            : "SELECT * FROM expenses WHERE category_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (categoryID == 0) {
                pstmt.setString(1, startDate);
                pstmt.setString(2, endDate);
            } else {
                pstmt.setInt(1, categoryID);
                pstmt.setString(2, startDate);
                pstmt.setString(3, endDate);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching filtered expenses: " + e.getMessage());
        }
        return expenses;
    }

    public boolean updateExpense(int id, double amount, int catId, String notes) {
        String sql = "UPDATE expenses SET amount = ?, category_id = ?, notes = ? WHERE expense_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, catId);
            pstmt.setString(3, notes);
            pstmt.setInt(4, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting expense: " + e.getMessage());
            return false;
        }
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense(
            rs.getDouble("amount"),
            rs.getInt("category_id"),
            rs.getString("notes")
        );
        e.setExpenseId(rs.getInt("expense_id"));
        e.setCycleId(rs.getInt("cycle_id"));

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
        return e;
    }
}