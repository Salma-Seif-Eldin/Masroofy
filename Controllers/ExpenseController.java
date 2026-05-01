package Controllers;
import Models.Expense;
import Database.TransactionDAO;
import java.util.List;

public class ExpenseController {
    private TransactionDAO transactionDAO;
    private BudgetManager budgetController;

    public ExpenseController(TransactionDAO transactionDAO, BudgetManager budgetController) {
        this.transactionDAO = transactionDAO;
        this.budgetController = budgetController;
    }

    /**
     * Handles creating a new expense and updating the safe daily limit.
     */
    // Inside ExpenseController.java
    public boolean processExpense(double amount, int category_id, String note) {
        if (amount <= 0) return false;

        Expense newExpense = new Expense(amount, category_id, note);
        // Link to the current cycle
        newExpense.setCycleId(budgetController.getCurrentCycle().getCycleId());

        boolean isSaved = transactionDAO.saveExpense(newExpense);

        if (isSaved) {
            // IMPORTANT: Update the budget manager's internal state
            budgetController.loadExistingBudget(); 
        }
        return isSaved;
    }
    /**
     * Fetches expenses based on filters (US #7 & US #8 enhancement).
     */
    public List<Expense> loadHistory(int categoryID, long start, long end) {
        return transactionDAO.getFilteredExpenses(categoryID, start, end);
    }

    /**
     * Combined Edit/Delete logic (US #8).
     * @param action "Edit" or "Delete"
     */
    public boolean modifyTransaction(int id, String action, Expense newData) {
        boolean success = false;

        if ("Delete".equalsIgnoreCase(action)) {
            success = transactionDAO.deleteExpense(id);
        } else if ("Edit".equalsIgnoreCase(action)) {
            if (newData.getAmount() <= 0) return false;
            success = transactionDAO.updateExpense(newData);
        }

        if (success) {
            // Any change to past data requires a full budget recalculation
            budgetController.recalculateLimits();
        }
        return success;
    }
    
    
}
