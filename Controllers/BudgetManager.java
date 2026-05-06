package Controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import Models.BudgetCycle;
import Models.DashboardModel;
import Models.Expense;
import Models.Category;
import Database.CycleDAO;
import Database.UserDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Central controller that manages all budget-related operations for the
 * application.
 * <p>
 * Coordinates between the database layer ({@link CycleDAO}, {@link UserDAO}),
 * the domain models ({@link BudgetCycle}, {@link Expense}), and the alert
 * system
 * ({@link AlertManager}). Maintains the currently active budget cycle and the
 * logged-in user's PIN in memory for the duration of the session.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class BudgetManager {

    /**
     * Constructs a new BudgetManager with fresh DAOs and alert manager.
     */
    public BudgetManager() {
    }

    private BudgetCycle currentCycle;
    private List<Expense> expenses = new ArrayList<>();
    private CycleDAO cycleDAO = new CycleDAO();
    private UserDAO userDAO = new UserDAO();
    private AlertManager alertManager = new AlertManager();
    private String currentPin;

    /**
     * Sets the PIN of the currently logged-in user.
     *
     * @param pin the user's 4-digit PIN string
     */
    public void setCurrentPin(String pin) {
        this.currentPin = pin;
    }

    /**
     * Returns the PIN of the currently logged-in user.
     *
     * @return the current user's PIN, or {@code null} if no user is logged in
     */
    public String getCurrentPin() {
        return currentPin;
    }

    /**
     * Returns the {@link AlertManager} used to trigger budget notifications.
     *
     * @return the alert manager instance
     */
    public AlertManager getAlertManager() {
        return alertManager;
    }

    /**
     * Registers a new user PIN in the database.
     *
     * @param pin the PIN to register
     * @return {@code true} if registration succeeded; {@code false} otherwise
     */
    public boolean registerPin(String pin) {
        return userDAO.registerPin(pin);
    }

    /**
     * Checks whether a given PIN is already registered in the database.
     *
     * @param pin the PIN to check
     * @return {@code true} if the PIN exists; {@code false} otherwise
     */
    public boolean pinExists(String pin) {
        return userDAO.pinExists(pin);
    }

    /**
     * Saves a PIN by registering it in the database.
     *
     * @param pin the PIN to save
     */
    public void savePin(String pin) {
        registerPin(pin);
    }

    /**
     * Retrieves the first PIN stored in the database.
     *
     * @return the first saved PIN string, or {@code null} if none exists
     */
    public String getSavedPin() {
        return userDAO.getFirstPin();
    }

    /**
     * Loads the most recent budget cycle and its expenses for the current user from
     * the database.
     * <p>
     * Does nothing if no user PIN is set. Calculates the remaining balance after
     * loading.
     * </p>
     */
    public void loadExistingBudget() {
        if (currentPin == null)
            return;
        currentCycle = cycleDAO.getLastSavedCycle(currentPin);
        if (currentCycle != null) {
            expenses = cycleDAO.getExpensesByCycle(currentCycle.getCycleId(), currentPin);
            currentCycle.calculateRemainingBalance(expenses);
            System.out.println("Loaded " + expenses.size() + " expenses.");
        }
    }

    /**
     * Builds and returns the dashboard data model for the current budget cycle.
     *
     * @return a {@link DashboardModel} containing summary data, or {@code null} if
     *         no cycle is active
     */
    public DashboardModel getDashboardData() {
        if (currentCycle == null)
            return null;

        double allowance = currentCycle.getTotalAllowance();

        double totalSpentCycle = 0;
        for (Expense e : expenses) {
            totalSpentCycle += e.getAmount();
        }

        double dailySpent = getDailySpent();
        double dailyLimit = getFixedDailyLimit();

        String color = "Green";
        double pct = (totalSpentCycle / allowance) * 100;
        if (pct >= 100)
            color = "Red";
        else if (pct >= 80)
            color = "Orange";

        return new DashboardModel(allowance, totalSpentCycle, dailySpent, dailyLimit, getPieChartData(), color);
    }

    /**
     * Creates and saves a new budget cycle for the current user.
     *
     * @param allowance the total budget allowance (must be positive)
     * @param start     the start date of the cycle
     * @param end       the end date of the cycle (must not be before start)
     * @return a result string: {@code "success"}, {@code "no_user"},
     *         {@code "invalid_allowance"}, {@code "invalid_dates"}, or
     *         {@code "db_error"}
     */
    public String startCycle(double allowance, Date start, Date end) {
        if (currentPin == null)
            return "no_user";
        if (allowance <= 0)
            return "invalid_allowance";
        if (start == null || end == null || end.before(start))
            return "invalid_dates";

        BudgetCycle newCycle = new BudgetCycle();
        newCycle.setUserPin(currentPin);
        newCycle.setTotalAllowance(allowance);
        newCycle.setStartDate(start);
        newCycle.setEndDate(end);

        boolean saved = cycleDAO.saveNewCycle(newCycle, currentPin);
        if (saved) {
            this.currentCycle = newCycle;
            this.expenses.clear();
            return "success";
        }

        return "db_error";
    }

    /**
     * Returns the currently active budget cycle.
     *
     * @return the current {@link BudgetCycle}, or {@code null} if none is active
     */
    public BudgetCycle getCurrentCycle() {
        return currentCycle;
    }

    /**
     * Returns the list of expenses recorded in the current cycle.
     *
     * @return the list of {@link Expense} objects
     */
    public List<Expense> getExpenses() {
        return expenses;
    }

    /**
     * Returns the total amount spent in the current cycle.
     *
     * @return the total spent, or {@code 0} if no cycle is active
     */
    public double getTotalSpent() {
        if (currentCycle == null)
            return 0;
        return currentCycle.getTotalAllowance() - getRemainingBudget();
    }

    /**
     * Returns the remaining budget for the current cycle after all expenses.
     *
     * @return the remaining balance, or {@code 0} if no cycle is active
     */
    public double getRemainingBudget() {
        if (currentCycle == null)
            return 0;
        return currentCycle.calculateRemainingBalance(expenses);
    }

    /**
     * Returns the fixed daily spending limit based on the total allowance and cycle
     * duration.
     *
     * @return the daily limit amount, or {@code 0.0} if no cycle is active
     */
    public double getFixedDailyLimit() {
        if (currentCycle == null)
            return 0.0;
        int totalDays = currentCycle.calculateTotalDays();
        if (totalDays <= 0)
            return 0.0;
        return currentCycle.getTotalAllowance() / totalDays;
    }

    /**
     * Returns the remaining daily budget for today after subtracting today's
     * spending.
     *
     * @return the remaining daily amount (minimum 0)
     */
    public double getTodayRemainingDailyLimit() {
        double fixedLimit = getFixedDailyLimit();
        double todaySpent = getDailySpent();
        return Math.max(0, fixedLimit - todaySpent);
    }

    /**
     * Checks whether a new expense of the given amount can be added without
     * exceeding any budget limit.
     *
     * @param amount the expense amount to check
     * @return {@code true} if the expense is within all limits; {@code false}
     *         otherwise
     */
    public boolean canAddExpense(double amount) {
        return getExpenseRejectionReason(amount) == null;
    }

    /**
     * Returns the reason why an expense of the given amount would be rejected,
     * or {@code null} if the expense is allowed.
     *
     * @param amount the expense amount to evaluate
     * @return {@code "no_cycle"} if no cycle is active,
     *         {@code "total_budget_exhausted"}
     *         if the total budget is insufficient, {@code "daily_limit_exceeded"}
     *         if
     *         today's limit would be breached, or {@code null} if the expense is
     *         allowed
     */
    public String getExpenseRejectionReason(double amount) {
        if (currentCycle == null)
            return "no_cycle";

        double totalRemaining = currentCycle.getTotalAllowance() - getTotalSpent();
        if (amount > totalRemaining)
            return "total_budget_exhausted";

        double dailyLimit = getFixedDailyLimit();
        double todaySpent = getDailySpent();
        if (dailyLimit > 0 && (todaySpent + amount) > dailyLimit)
            return "daily_limit_exceeded";

        return null;
    }

    /**
     * Calculates the total amount spent today across all expenses.
     *
     * @return today's total spending amount
     */
    public double getDailySpent() {
        double todayTotal = 0;
        Date today = new Date();
        for (Expense e : expenses) {
            if (isSameDay(e.getTimestamp(), today))
                todayTotal += e.getAmount();
        }
        return todayTotal;
    }

    /**
     * Returns the percentage of the total allowance that has been spent.
     *
     * @return the spending percentage (0–100), or {@code 0} if no cycle is active
     */
    public double getSpentPercentage() {
        if (currentCycle == null || currentCycle.getTotalAllowance() <= 0)
            return 0;
        double spent = currentCycle.getTotalAllowance() - getRemainingBudget();
        return (spent / currentCycle.getTotalAllowance()) * 100;
    }

    /**
     * Aggregates expenses by category name and returns a map suitable for pie chart
     * display.
     *
     * @return a map of category names to their total spending amounts
     */
    public Map<String, Double> getPieChartData() {
        Map<String, Double> data = new HashMap<>();
        if (expenses == null)
            return data;
        for (Expense e : expenses) {
            String name = Category.getNameById(e.getCategoryId());
            data.put(name, data.getOrDefault(name, 0.0) + e.getAmount());
        }
        return data;
    }

    /**
     * Calculates the number of days remaining until the given end date (inclusive).
     *
     * @param endDate the end date of the budget cycle
     * @return the number of days remaining, minimum 1
     */
    public long calculateDaysRemaining(Date endDate) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate end = LocalDate.parse(
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(endDate));
            long days = ChronoUnit.DAYS.between(today, end) + 1;
            return (days <= 0) ? 1 : days;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Recalculates the remaining balance for the current cycle based on the current
     * expense list.
     */
    public void refreshBudgetState() {
        if (currentCycle != null) {
            currentCycle.calculateRemainingBalance(this.expenses);
        }
    }

    /**
     * Adds an expense to the in-memory list and triggers budget alert checks.
     *
     * @param exp the {@link Expense} to add
     */
    public void addExpense(Expense exp) {
        this.expenses.add(exp);

        double totalSpent = getTotalSpent();
        double totalAllowance = currentCycle.getTotalAllowance();
        double todaySpent = getDailySpent();
        double dailyLimit = getFixedDailyLimit();

        alertManager.monitorBudgetTotalSpent(totalSpent, totalAllowance);
        alertManager.monitorDailyLimit(todaySpent, dailyLimit);
    }

    /**
     * Determines whether two dates fall on the same calendar day.
     *
     * @param d1 the first date
     * @param d2 the second date
     * @return {@code true} if both dates represent the same day; {@code false}
     *         otherwise
     */
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null)
            return false;
        java.time.LocalDate date1 = new java.sql.Date(d1.getTime()).toLocalDate();
        java.time.LocalDate date2 = new java.sql.Date(d2.getTime()).toLocalDate();
        return date1.equals(date2);
    }
}