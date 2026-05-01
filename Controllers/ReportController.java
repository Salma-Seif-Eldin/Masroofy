package Controllers;

import Models.Expense;
import Models.BudgetCycle;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ReportController {

    private final BudgetManager budgetManager;

    public ReportController(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    
    public Map<String, Double> getSpendingByCategory() {
        Map<String, Double> report = new HashMap<>();
        List<Expense> expenses = budgetManager.getExpenses();

        if (expenses == null) return report;

        for (Expense e : expenses) {
            String categoryName = getCategoryName(e.getCategoryId());
            report.put(categoryName, report.getOrDefault(categoryName, 0.0) + e.getAmount());
        }
        return report;
    }

    
    public String generateSummaryReport() {
        BudgetCycle cycle = budgetManager.getCurrentCycle();
        if (cycle == null) return "No active budget cycle found.";

        double totalSpent = cycle.getTotalAllowance() - budgetManager.getRemainingBudget();
        double percentage = budgetManager.getSpentPercentage();

        StringBuilder summary = new StringBuilder();
        summary.append("--- Financial Report ---\n");
        summary.append(String.format("Total Allowance: %.2f EGP\n", cycle.getTotalAllowance()));
        summary.append(String.format("Total Spent: %.2f EGP\n", totalSpent));
        summary.append(String.format("Budget Used: %.1f%%\n", percentage));
        summary.append(String.format("Remaining: %.2f EGP\n", budgetManager.getRemainingBudget()));
        
        if (cycle.isEightyPercentReached()) {
            summary.append("\n⚠️ WARNING: You have spent over 80% of your budget!");
        }

        return summary.toString();
    }

    private String getCategoryName(int id) {
        return switch (id) {
            case 1 -> "Food";
            case 2 -> "Transport";
            case 3 -> "Shopping";
            case 4 -> "Health";
            case 5 -> "Education";
            case 6 -> "Entertainment";
            default -> "Other";
        };
    }
}