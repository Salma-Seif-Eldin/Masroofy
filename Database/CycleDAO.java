package Database;

import Models.BudgetCycle;
import Models.Expense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CycleDAO {

 

    //  
    public boolean saveNewCycle(BudgetCycle cycle) {
        String sql = "INSERT INTO budget_cycles(total_allowance, start_date, end_date) " +
                     "VALUES(?,?,?)";  // Inserts a new budget cycle row into the database

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            // RETURN_GENERATED_KEYS tells SQLite to give us back
            // the auto-generated cycle_id after insert
            pstmt.setDouble(1, cycle.getTotalAllowance());
            pstmt.setString(2, cycle.getStartDate().toString());
            pstmt.setString(3, cycle.getEndDate().toString());
            pstmt.executeUpdate();

          
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                cycle.setCycleId(keys.getInt(1));
                 // Store the database-generated ID back into our object
                // So BudgetManager knows the cycleId for linking expenses
            }

            System.out.println("Cycle saved with ID: " + cycle.getCycleId());
            return true;

        } catch (Exception e) {
            System.out.println("Error saving cycle: " + e.getMessage());
            return false;
        }
    }

    
    public BudgetCycle getLastSavedCycle() {
        // Loads the most recent cycle from database on app startup
        // Returns null if no cycle exists yet
        String sql = "SELECT * FROM budget_cycles ORDER BY cycle_id DESC LIMIT 1";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
                 // ResultSet = the rows returned by SELECT

            if (rs.next()) {
                BudgetCycle cycle = new BudgetCycle();
                cycle.setCycleId(rs.getInt("cycle_id"));
                    // Read cycle_id column from result
              
                double allowance = rs.getDouble("total_allowance");
                Date start = Date.valueOf(rs.getString("start_date"));
                Date end   = Date.valueOf(rs.getString("end_date"));

                // Read and convert date strings back to Date objects
                cycle.initialize(allowance, start, end);
               // Rebuild the cycle object with saved data
                return cycle;
            }

        } catch (Exception e) {
            System.out.println("Error loading cycle: " + e.getMessage());
        }
        return null; // no saved cycle found
    }

  
    //expense methods

      // Saves a new expense to the database
    // Links it to the current cycle via cycle_id
    public boolean insertExpense(Expense expense) {
        String sql = "INSERT INTO expenses(cycle_id, amount, category_id, notes, timestamp) " + "VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, expense.getCycleId());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setInt(3, expense.getCategoryId());
            pstmt.setString(4, expense.getNotes());
            pstmt.setString(5, expense.getTimestamp().toString());
            pstmt.executeUpdate();

          
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                expense.setExpenseId(keys.getInt(1));
            }
            return true;

        } catch (Exception e) {
            System.out.println("Error inserting expense: " + e.getMessage());
            return false;
        }
    }

    public List<Expense> getExpensesByCycle(int cycleId) {
        // loads  expenses for a specific cycle
        // called on startup to restore expense history
        String sql = "SELECT * FROM expenses WHERE cycle_id = ?";
        List<Expense> expenses = new ArrayList<>();
         // Start with empty list, fill it from database

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cycleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                 // Loop through every expense row returned
                Expense e = new Expense(
                    rs.getDouble("amount"),
                    rs.getInt("category_id"),
                    rs.getString("notes")
                );
                e.setExpenseId(rs.getInt("expense_id"));
                e.setCycleId(rs.getInt("cycle_id"));
                expenses.add(e);
                 // Build Expense object from row and add to list
            }

        } catch (Exception e) {
            System.out.println("Error loading expenses: " + e.getMessage());
        }
        return expenses;
        // Return full list (empty if none found)
    }

    
    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET amount=?, category_id=?, notes=? " + "WHERE expense_id=?";
            // Updates an existing expense row in the database
        // Called by BudgetManager.editExpense()

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, expense.getAmount());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setString(3, expense.getNotes());
            pstmt.setInt(4, expense.getExpenseId());
              // WHERE clause — find the right row
            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error updating expense: " + e.getMessage());
            return false;
        }
    }

    
    public boolean deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id=?";
          // removes an expense from the database
        //  by BudgetManager.deleteExpense()

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, expenseId);
            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error deleting expense: " + e.getMessage());
            return false;
        }
    }
    /**
 * MEMBER 1 FILE: CycleDAO.java
 * Updated by Member 4 (The Glue) to support "Refund" on delete.
 */
    public void updateBalance(double refundAmount) {
        // This SQL adds the refundAmount to the current 'remaining' balance
        // We target the 'active' cycle (where the ID is the latest or flagged as active)
        String sql = "UPDATE budget_cycles SET remaining = remaining + ? WHERE id = (SELECT MAX(id) FROM budget_cycles)";

        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, refundAmount);
            pstmt.executeUpdate();
            
            System.out.println("Balance updated: " + refundAmount + " added back.");
            
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }
    
}