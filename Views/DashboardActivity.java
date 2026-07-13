package Views;

import Controllers.BudgetManager;
import Controllers.ReportController;
import Models.DashboardModel;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

/**
 * The main dashboard screen of the Masroofy application.
 * <p>
 * Displays budget summary information including the total allowance, remaining
 * balance, daily spending, a progress bar, and a pie chart of spending by category.
 * Provides navigation to the expense entry and history screens, and supports
 * generating a budget summary report.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class DashboardActivity extends JPanel {

    private JLabel tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent, tvStatus;
    private JLabel tvTodayRemaining;
    private JProgressBar pbBudgetProgress;
    private final BudgetManager budgetManager;
    private ReportController reportController;
    private SpendingChart spendingChart;

    /**
     * Constructs the DashboardActivity and initializes all UI components.
     * <p>
     * Sets up the alert callback so budget warnings are shown as dialog messages.
     * </p>
     *
     * @param manager the {@link BudgetManager} providing budget data and operations
     */
    public DashboardActivity(BudgetManager manager) {
        this.budgetManager = manager;
        this.reportController = new ReportController(budgetManager);

        budgetManager.getAlertManager().setCallback((title, message) -> {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 25, 47));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initViews();
        refreshUI();
    }

    /**
     * Initializes and adds all UI components to the panel including labels,
     * progress bar, spending chart, and action buttons.
     */
    private void initViews() {
        tvStatus         = new JLabel("Checking budget status...");
        tvAllowance      = new JLabel("Total Allowance: 0.00");
        tvRemaining      = new JLabel("Remaining: 0.00");
        tvDailyLimit     = new JLabel("Daily Limit: 0.00");
        tvTodayRemaining = new JLabel("Today Remaining: 0.00");
        tvDailySpent     = new JLabel("Spent Today: 0.00");

        applyWhiteForeground(tvStatus, tvAllowance, tvRemaining, tvDailyLimit,
                             tvTodayRemaining, tvDailySpent);

        pbBudgetProgress = new JProgressBar(0, 100);
        pbBudgetProgress.setStringPainted(true);
        pbBudgetProgress.setForeground(new Color(212, 175, 55));

        spendingChart = new SpendingChart();
        spendingChart.setPreferredSize(new Dimension(400, 200));
        JLabel lblChartTitle = new JLabel("Spending by Category:");
        lblChartTitle.setForeground(Color.WHITE);

        JButton btnAddExpense = new JButton(" Add Expense");
        JButton btnHistory    = new JButton(" View History");
        JButton btnReport     = new JButton(" Generate Report");

        styleButton(btnAddExpense);
        styleButton(btnHistory);
        styleButton(btnReport);

        btnAddExpense.addActionListener(e -> {
            ExpensesEntryActivity entry = new ExpensesEntryActivity(budgetManager);
            entry.setVisible(true);
            entry.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent ev) {
                    refreshUI();
                }
            });
        });

        btnHistory.addActionListener(e -> new HistoryActivity(budgetManager, this).setVisible(true));

        btnReport.addActionListener(e -> {
            String report = reportController.generateSummaryReport();
            JOptionPane.showMessageDialog(this, report, "Report Summary",
                JOptionPane.INFORMATION_MESSAGE);
        });

        add(tvStatus);
        add(Box.createVerticalStrut(10));
        add(tvAllowance);
        add(tvRemaining);
        add(Box.createVerticalStrut(15));
        add(pbBudgetProgress);
        add(Box.createVerticalStrut(15));
        add(tvDailyLimit);
        add(tvTodayRemaining);
        add(tvDailySpent);
        add(Box.createVerticalStrut(20));
        add(lblChartTitle);
        add(spendingChart);
        add(Box.createVerticalStrut(20));
        add(btnAddExpense);
        add(Box.createVerticalStrut(10));
        add(btnHistory);
        add(Box.createVerticalStrut(10));
        add(btnReport);
    }

    /**
     * Refreshes all UI labels, progress bar, and chart with the latest budget data.
     * <p>
     * Applies color coding to the today-remaining label based on how close the user
     * is to their daily limit.
     * </p>
     */
    public void refreshUI() {
        DashboardModel uiModel = budgetManager.getDashboardData();
        if (uiModel == null) return;

        tvAllowance.setText(String.format("Total Allowance: %.2f EGP", uiModel.getTotalAllowance()));
        tvRemaining.setText(String.format("Remaining: %.2f EGP", uiModel.getRemainingBudget()));
        tvDailyLimit.setText(String.format("Daily Limit: %.2f EGP", uiModel.getSafeDailyLimit()));
        tvDailySpent.setText(String.format("Daily Spent: %.2f EGP", uiModel.getDailySpent()));

        double todayRemaining = budgetManager.getTodayRemainingDailyLimit();
        tvTodayRemaining.setText(String.format("Today Remaining: %.2f EGP", todayRemaining));

        if (todayRemaining <= 0) {
            tvTodayRemaining.setForeground(Color.RED);
        } else if (todayRemaining < budgetManager.getFixedDailyLimit() * 0.2) {
            tvTodayRemaining.setForeground(Color.ORANGE);
        } else {
            tvTodayRemaining.setForeground(new Color(50, 205, 50));
        }

        int progress = Math.min(100, (int) uiModel.getSpendingPercentage());
        pbBudgetProgress.setValue(progress);

        tvStatus.setText(uiModel.getStatusMessage());
        tvStatus.setForeground(uiModel.getStatusColor());

        Map<String, Double> categoryData = budgetManager.getPieChartData();
        if (spendingChart != null) {
            spendingChart.setData(categoryData);
            spendingChart.repaint();
        }
    }

    /**
     * Applies white foreground color to all provided labels.
     *
     * @param labels the labels to style
     */
    private void applyWhiteForeground(JLabel... labels) {
        for (JLabel l : labels) l.setForeground(Color.WHITE);
    }

    /**
     * Applies a consistent gold-on-black style to the given button.
     *
     * @param btn the button to style
     */
    private void styleButton(JButton btn) {
        btn.setBackground(new Color(212, 175, 55));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}