package Controllers;

import Models.Expense;
import Models.BudgetCycle;
import Models.Category;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller responsible for generating budget reports and spending summaries.
 * <p>
 * Uses data from {@link BudgetManager} to produce category breakdowns and
 * human-readable financial summary reports for the current budget cycle.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class ReportController {

    private final BudgetManager budgetManager;

    /**
     * Constructs a ReportController with the given budget manager.
     *
     * @param budgetManager the {@link BudgetManager} providing expense and cycle data
     */
    public ReportController(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    /**
     * Aggregates total spending per category for the current budget cycle.
     *
     * @return a map of category names to their total spending amounts;
     *         returns an empty map if no expenses exist
     */
    public Map<String, Double> getSpendingByCategory() {
        Map<String, Double> report = new HashMap<>();
        List<Expense> expenses = budgetManager.getExpenses();

        if (expenses == null) return report;

        for (Expense e : expenses) {
            String categoryName = Category.getNameById(e.getCategoryId());
            report.put(categoryName, report.getOrDefault(categoryName, 0.0) + e.getAmount());
        }
        return report;
    }

    /**
     * Generates a formatted financial summary report for the current budget cycle.
     * <p>
     * Includes total allowance, total spent, percentage used, remaining balance,
     * and a warning if over 80% of the budget has been consumed.
     * </p>
     *
     * @return a multi-line string report, or a message indicating no active cycle
     */
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
}