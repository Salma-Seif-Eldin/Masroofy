package Controllers;

import Models.Expense;
import Database.TransactionDAO;
import java.util.List;

/**
 * Controller responsible for processing expense transactions.
 * <p>
 * Acts as the intermediary between the UI layer and the database/budget logic.
 * Validates expense input, enforces budget rules via {@link BudgetManager},
 * persists approved expenses through {@link TransactionDAO}, and provides
 * history and filtering capabilities.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class ExpenseController {

    private TransactionDAO transactionDAO;
    private BudgetManager budgetController;

    /**
     * Constructs an ExpenseController with explicit DAO and manager dependencies.
     *
     * @param transactionDAO  the DAO used to persist expense records
     * @param budgetController the {@link BudgetManager} providing budget rules and state
     */
    public ExpenseController(TransactionDAO transactionDAO, BudgetManager budgetController) {
        this.transactionDAO = transactionDAO;
        this.budgetController = budgetController;
    }

    /**
     * Factory method that creates an ExpenseController with a default {@link TransactionDAO}.
     *
     * @param budgetManager the {@link BudgetManager} to associate with this controller
     * @return a new {@code ExpenseController} instance
     */
    public static ExpenseController createFor(BudgetManager budgetManager) {
        return new ExpenseController(new TransactionDAO(), budgetManager);
    }

    /**
     * Validates and processes a new expense entry.
     * <p>
     * Checks that the amount is positive, verifies budget limits, saves the expense
     * to the database, and updates the in-memory budget state.
     * </p>
     *
     * @param amount      the expense amount (must be greater than 0)
     * @param category_id the ID of the spending category
     * @param note        an optional description of the expense
     * @return an {@link ExpenseResult} indicating success or the reason for failure
     */
    public ExpenseResult processExpense(double amount, int category_id, String note) {
        if (amount <= 0) {
            return new ExpenseResult(false, "Please enter a valid amount greater than zero.", "invalid_amount");
        }

        String rejectionReason = budgetController.getExpenseRejectionReason(amount);

        if ("daily_limit_exceeded".equals(rejectionReason)) {
            return new ExpenseResult(false,
                "Cannot add expense. This would exceed your daily limit of " +
                String.format("%.2f", budgetController.getFixedDailyLimit()) + " EGP.",
                "daily_limit_exceeded");
        }

        Expense exp = new Expense(amount, category_id, note);
        exp.setCycleId(budgetController.getCurrentCycle().getCycleId());
        exp.setUserPin(budgetController.getCurrentPin());

        boolean saved = transactionDAO.saveExpense(exp);
        if (saved) {
            budgetController.addExpense(exp);
            return new ExpenseResult(true, "Transaction saved successfully", null);
        } else {
            return new ExpenseResult(false, "Failed to save transaction in database", "db_error");
        }
    }

    /**
     * Returns the full expense history for the current budget cycle.
     *
     * @return a list of all {@link Expense} objects in the current cycle
     */
    public List<Expense> loadHistory() {
        return budgetController.getExpenses();
    }

    /**
     * Edits or deletes an existing expense transaction by ID.
     * <p>
     * After a successful modification, the budget state is reloaded from the database.
     * </p>
     *
     * @param id          the ID of the expense to modify
     * @param action      the action to perform: {@code "Edit"} or {@code "Delete"}
     * @param updatedData the updated {@link Expense} data (required for Edit; ignored for Delete)
     * @return {@code true} if the operation succeeded; {@code false} otherwise
     */
    public boolean modifyTransaction(int id, String action, Expense updatedData) {
        boolean success = false;

        if ("Edit".equals(action) && updatedData != null) {
            success = transactionDAO.updateExpense(id, updatedData.getAmount(),
                          updatedData.getCategoryId(), updatedData.getNotes());
        } else if ("Delete".equals(action)) {
            success = transactionDAO.deleteExpense(id);
        }

        if (success) {
            budgetController.loadExistingBudget();
        }
        return success;
    }

    /**
     * Returns a filtered list of expenses based on category and date range.
     *
     * @param categoryID the category ID to filter by (use 0 or -1 for all categories)
     * @param startDate  the start date string in {@code yyyy-MM-dd} format
     * @param endDate    the end date string in {@code yyyy-MM-dd} format
     * @return a filtered list of {@link Expense} objects matching the criteria
     */
    public List<Expense> filterHistory(int categoryID, String startDate, String endDate) {
        return transactionDAO.getFilteredExpenses(categoryID, startDate, endDate);
    }

    /**
     * Represents the outcome of processing an expense.
     * <p>
     * Carries a success flag, a user-facing message, an optional warning,
     * and a rejection type string used to differentiate error cases in the UI.
     * </p>
     */
    public static class ExpenseResult {
        private final boolean success;
        private final String message;
        private final String warning;
        private final String rejectionType;

        /**
         * Constructs an ExpenseResult without a warning message.
         *
         * @param success       {@code true} if the expense was processed successfully
         * @param message       a user-facing message describing the outcome
         * @param rejectionType a code identifying the rejection reason, or {@code null} on success
         */
        public ExpenseResult(boolean success, String message, String rejectionType) {
            this.success = success;
            this.message = message;
            this.warning = null;
            this.rejectionType = rejectionType;
        }

        /**
         * Constructs an ExpenseResult with an optional warning message.
         *
         * @param success       {@code true} if the expense was processed successfully
         * @param message       a user-facing message describing the outcome
         * @param warning       an optional warning message shown alongside success
         * @param rejectionType a code identifying the rejection reason, or {@code null} on success
         */
        public ExpenseResult(boolean success, String message, String warning, String rejectionType) {
            this.success = success;
            this.message = message;
            this.warning = warning;
            this.rejectionType = rejectionType;
        }

        /**
         * Returns whether the expense was successfully processed.
         *
         * @return {@code true} if successful
         */
        public boolean isSuccess() { return success; }

        /**
         * Returns the primary user-facing message for this result.
         *
         * @return the result message
         */
        public String getMessage() { return message; }

        /**
         * Returns an optional warning message associated with this result.
         *
         * @return the warning string, or {@code null} if none
         */
        public String getWarning() { return warning; }

        /**
         * Returns the rejection type code for this result.
         *
         * @return the rejection type string, or {@code null} if the expense succeeded
         */
        public String getRejectionType() { return rejectionType; }

        /**
         * Returns whether this result carries a non-empty warning message.
         *
         * @return {@code true} if a warning is present
         */
        public boolean hasWarning() { return warning != null && !warning.isEmpty(); }
    }
}