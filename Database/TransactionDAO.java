package Database;

import Models.Expense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class TransactionDAO {

    public boolean saveExpense(Expense expense) {
    String sql = "INSERT INTO expenses (amount, category_id, notes, date, cycle_id) VALUES (?, ?, ?, DATE('now'), ?)";

    try (Connection conn = DatabaseManager.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setDouble(1, expense.getAmount());
        pstmt.setInt(2, expense.getCategoryId());
        pstmt.setString(3, expense.getNotes());
        pstmt.setInt(4, expense.getCycleId());
        
        pstmt.executeUpdate();
        return true;
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

    public List<Expense> getFilteredExpenses(int categoryID, long start, long end) {
        List<Expense> expenses = new ArrayList<>();
        String sql = (categoryID == 0) 
            ? "SELECT * FROM expenses WHERE date >= ? AND date <= ? ORDER BY date DESC"
            : "SELECT * FROM expenses WHERE category_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (categoryID == 0) {
                pstmt.setLong(1, start);
                pstmt.setLong(2, end);
            } else {
                pstmt.setInt(1, categoryID);
                pstmt.setLong(2, start);
                pstmt.setLong(3, end);
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

    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, notes = ?, category_id = ? WHERE expense_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, expense.getAmount());
            pstmt.setString(2, expense.getNotes());
            pstmt.setInt(3, expense.getCategoryId());
            pstmt.setInt(4, expense.getExpenseId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating expense: " + e.getMessage());
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
        Date date = new Date(rs.getLong("date"));
        
        Expense e = new Expense(
            rs.getDouble("amount"),
            rs.getInt("category_id"),
            rs.getString("notes")
        );
        e.setExpenseId(rs.getInt("expense_id"));
        e.setTimestamp(date);
        e.setCycleId(rs.getInt("cycle_id"));
        return e;
    }
}