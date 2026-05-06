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

    public static ExpenseController createFor(BudgetManager budgetManager) {
        return new ExpenseController(new TransactionDAO(), budgetManager);
    }

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

    public List<Expense> loadHistory() {
        return budgetController.getExpenses();
    }

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

    public List<Expense> filterHistory(int categoryID, String startDate, String endDate) {
        return transactionDAO.getFilteredExpenses(categoryID, startDate, endDate);
    }

    public static class ExpenseResult {
        private final boolean success;
        private final String message;
        private final String warning;
        private final String rejectionType;

        public ExpenseResult(boolean success, String message, String rejectionType) {
            this.success = success;
            this.message = message;
            this.warning = null;
            this.rejectionType = rejectionType;
        }

        public ExpenseResult(boolean success, String message, String warning, String rejectionType) {
            this.success = success;
            this.message = message;
            this.warning = warning;
            this.rejectionType = rejectionType;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getWarning() { return warning; }
        public String getRejectionType() { return rejectionType; }
        public boolean hasWarning() { return warning != null && !warning.isEmpty(); }
    }
}