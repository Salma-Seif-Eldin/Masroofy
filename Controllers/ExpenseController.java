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

    // ======================================================================
    // NEW: Factory method so Views don't create TransactionDAO directly
    // ======================================================================
    public static ExpenseController createFor(BudgetManager budgetManager) {
        return new ExpenseController(new TransactionDAO(), budgetManager);
    }

    public ExpenseResult processExpense(double amount, int category_id, String note) {
        if (amount <= 0) {
            return new ExpenseResult(false, "Amount must be greater than 0.", "invalid_amount");
        }
        
        if (budgetController.getCurrentCycle() == null) {
            return new ExpenseResult(false, "No active budget cycle.", "no_cycle");
        }

        String rejectionReason = budgetController.getExpenseRejectionReason(amount);
        
        if (rejectionReason != null) {
            switch (rejectionReason) {
                case "budget":
                    double remainingBudget = budgetController.getRemainingBudget();
                    return new ExpenseResult(false, 
                        String.format("Cannot add EGP %.2f. Only EGP %.2f remaining in total budget!", 
                            amount, remainingBudget), 
                        "budget_exceeded");
                    
                case "daily_limit":
                    double todayRemaining = budgetController.getTodayRemainingDailyLimit();
                    double dailyLimit = budgetController.getFixedDailyLimit();
                    return new ExpenseResult(false, 
                        String.format("Cannot add EGP %.2f. You only have EGP %.2f remaining today (Daily limit: EGP %.2f)!", 
                            amount, todayRemaining, dailyLimit), 
                        "daily_limit_exceeded");
                    
                default:
                    return new ExpenseResult(false, "Cannot add this expense.", rejectionReason);
            }
        }

        Expense newExpense = new Expense(amount, category_id, note);
        newExpense.setCycleId(budgetController.getCurrentCycle().getCycleId());
        newExpense.setUserPin(budgetController.getCurrentPin());

        boolean isSaved = transactionDAO.saveExpense(newExpense);

        if (isSaved) {
            budgetController.loadExistingBudget();
            
            double pct = budgetController.getSpentPercentage();
            String warning = null;
            if (pct >= 100) {
                warning = "🚨 WARNING: You have exhausted your entire budget!";
            } else if (pct >= 80) {
                warning = String.format("⚠️ WARNING: You have used %.1f%% of your budget!", pct);
            }
            
            return new ExpenseResult(true, "Expense saved successfully!", warning, null);
        } else {
            return new ExpenseResult(false, "Database error: Could not save expense.", "db_error");
        }
    }

    public List<Expense> loadHistory() {
        return budgetController.getExpenses();
    }

    public boolean modifyTransaction(int id, String action, Expense updatedData) {
        if (action.equals("Edit") && updatedData != null) {
            boolean success = transactionDAO.updateExpense(id, updatedData.getAmount(), 
                                                           updatedData.getCategoryId(), 
                                                           updatedData.getNotes());
            
            if (success) {
                budgetController.loadExistingBudget(); 
                return true;
            }
        } else if (action.equals("Delete")) {
            boolean success = transactionDAO.deleteExpense(id);
            if (success) {
                budgetController.loadExistingBudget();
                return true;
            }
        }
        return false;
    }

    public List<Expense> filterHistory(int categoryID, String startDate, String endDate) {
        return transactionDAO.getFilteredExpenses(categoryID, startDate, endDate);
    }

    // ======================================================================
    // Result class
    // ======================================================================
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