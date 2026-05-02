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

    public boolean processExpense(double amount, int category_id, String note) {
        if (amount <= 0) return false;
        if (budgetController.getCurrentCycle() == null) return false;

        Expense newExpense = new Expense(amount, category_id, note);
        newExpense.setCycleId(budgetController.getCurrentCycle().getCycleId());

        boolean isSaved = transactionDAO.saveExpense(newExpense);

        if (isSaved) {
            budgetController.loadExistingBudget(); 
        }
        return isSaved;
    }

    public List<Expense> loadHistory() {
        return budgetController.getExpenses();
    }

    public boolean modifyTransaction(int id, String action, Expense newData) {
        boolean success = false;

        if ("Delete".equalsIgnoreCase(action)) {
            success = transactionDAO.deleteExpense(id);
        } else if ("Edit".equalsIgnoreCase(action)) {
            if (newData == null || newData.getAmount() <= 0) return false;
            success = transactionDAO.updateExpense(newData);
        }

        if (success) {
            budgetController.loadExistingBudget();
        }
        return success;
    }
}