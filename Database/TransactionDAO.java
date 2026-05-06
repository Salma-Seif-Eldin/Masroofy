package Database;

import Models.Expense;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) for managing expense transaction records in the
 * database.
 * <p>
 * Provides full CRUD operations for expenses: saving new records, retrieving
 * all
 * or filtered expenses, updating existing entries, and deleting records by ID.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class TransactionDAO {

    /**
     * Constructs a new TransactionDAO for expense persistence operations.
     */
    public TransactionDAO() {
    }

    /**
     * Inserts a new expense record into the database using today's date.
     *
     * @param expense the {@link Expense} to persist (must have amount, categoryId,
     *                notes, cycleId, and userPin set)
     * @return {@code true} if the record was inserted successfully; {@code false}
     *         otherwise
     */
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

    /**
     * Retrieves all expense records from the database, ordered by date descending.
     *
     * @return a list of all {@link Expense} objects; empty list if none exist
     */
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

    /**
     * Retrieves a filtered list of expenses based on category and date range.
     * <p>
     * If {@code categoryID} is {@code 0}, all categories are included.
     * </p>
     *
     * @param categoryID the category ID to filter by; use {@code 0} for all
     *                   categories
     * @param startDate  the start date string in {@code yyyy-MM-dd} format
     *                   (inclusive)
     * @param endDate    the end date string in {@code yyyy-MM-dd} format
     *                   (inclusive)
     * @return a filtered list of {@link Expense} objects; empty list if none match
     */
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

    /**
     * Updates the amount, category, and notes of an existing expense by its ID.
     *
     * @param id     the ID of the expense to update
     * @param amount the new expense amount
     * @param catId  the new category ID
     * @param notes  the new notes string
     * @return {@code true} if at least one row was updated; {@code false} otherwise
     */
    public boolean updateExpense(int id, double amount, int catId, String notes) {
        String sql = "UPDATE expenses SET amount = ?, category_id = ?, notes = ? WHERE expense_id = ?";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, catId);
            pstmt.setString(3, notes);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an expense record from the database by its ID.
     *
     * @param id the ID of the expense to delete
     * @return {@code true} if the record was deleted; {@code false} otherwise
     */
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

    /**
     * Maps a database {@link ResultSet} row to an {@link Expense} object.
     * Falls back to the current date if the stored date string cannot be parsed.
     *
     * @param rs the {@link ResultSet} positioned at the row to map
     * @return the populated {@link Expense} object
     * @throws SQLException if a database access error occurs
     */
    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense(
                rs.getDouble("amount"),
                rs.getInt("category_id"),
                rs.getString("notes"));
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