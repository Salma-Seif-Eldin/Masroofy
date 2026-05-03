package Views;

import Controllers.BudgetManager;
import Controllers.ReportController;
import Models.DashboardModel;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class DashboardActivity extends JPanel {

    private JLabel tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent, tvStatus;
    private JLabel tvTodayRemaining;  // NEW: Show today's remaining limit
    private JProgressBar pbBudgetProgress;
    private final BudgetManager budgetManager;
    private ReportController reportController;
    private SpendingChart spendingChart;

    public DashboardActivity(BudgetManager manager) {
        this.budgetManager = manager;
        this.reportController = new ReportController(budgetManager);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 25, 47));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initViews();
        refreshUI();
    }

    private void initViews() {
        tvStatus    = new JLabel("Checking budget status...");
        tvAllowance = new JLabel("Total Allowance: 0.00");
        tvRemaining = new JLabel("Remaining: 0.00");
        tvDailyLimit = new JLabel("Daily Limit: 0.00");
        tvTodayRemaining = new JLabel("Today Remaining: 0.00");  // NEW
        tvDailySpent = new JLabel("Spent Today: 0.00");

        applyWhiteForeground(tvStatus, tvAllowance, tvRemaining, tvDailyLimit, 
                             tvTodayRemaining, tvDailySpent);

        pbBudgetProgress = new JProgressBar(0, 100);
        pbBudgetProgress.setStringPainted(true);
        pbBudgetProgress.setForeground(new Color(212, 175, 55));

        spendingChart = new SpendingChart();
        spendingChart.setPreferredSize(new Dimension(400, 200));
        JLabel lblChartTitle = new JLabel("Spending by Category:");
        lblChartTitle.setForeground(Color.WHITE);

        JButton btnAddExpense = new JButton("➕ Add Expense");
        JButton btnHistory    = new JButton("📋 View History");
        JButton btnReport     = new JButton("📊 Generate Report");

        styleButton(btnAddExpense);
        styleButton(btnHistory);
        styleButton(btnReport);

        btnAddExpense.addActionListener(e -> {
            ExpensesEntryActivity entry = new ExpensesEntryActivity(budgetManager);
            entry.setVisible(true);
            entry.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent ev) {
                    budgetManager.loadExistingBudget();
                    refreshUI();
                }
            });
        });

        btnHistory.addActionListener(e -> new HistoryActivity(budgetManager).setVisible(true));

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
        add(tvTodayRemaining);  // NEW
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

    public void refreshUI() {
        DashboardModel uiModel = budgetManager.getDashboardData();

        if (uiModel == null) {
            tvStatus.setText("No active cycle found.");
            return;
        }

        tvAllowance.setText(String.format("Total Allowance: %.2f EGP", uiModel.getTotalAllowance()));
        tvRemaining.setText(String.format("Remaining: %.2f EGP", uiModel.getRemainingBudget()));
        tvDailyLimit.setText(String.format("Daily Limit: %.2f EGP", uiModel.getSafeDailyLimit()));
        tvDailySpent.setText(String.format("Spent Today: %.2f EGP", uiModel.getTotalSpent()));
        
        // NEW: Show today's remaining daily limit
        double todayRemaining = budgetManager.getTodayRemainingDailyLimit();
        tvTodayRemaining.setText(String.format("Today Remaining: %.2f EGP", todayRemaining));
        
        // Color the today remaining label based on status
        if (todayRemaining <= 0) {
            tvTodayRemaining.setForeground(Color.RED);
        } else if (todayRemaining < budgetManager.getFixedDailyLimit() * 0.2) {
            tvTodayRemaining.setForeground(Color.ORANGE);
        } else {
            tvTodayRemaining.setForeground(new Color(50, 205, 50));
        }

        int progress = Math.min(100, (int) uiModel.getSpendingPercentage());
        pbBudgetProgress.setValue(progress);

        updateStatusColor(uiModel.getStatusColor());

        // FIX: NO MORE PERSISTENT ALERT HERE!
        // Alerts are now handled in ExpensesEntryActivity BEFORE saving

        Map<String, Double> categoryData = budgetManager.getPieChartData();
        if (spendingChart != null) {
            spendingChart.setData(categoryData);
            spendingChart.repaint();
        }
    }

    private void updateStatusColor(String color) {
        switch (color.toLowerCase()) {
            case "red":
                tvStatus.setText("⚠️ Critical: Budget Exceeded!");
                tvStatus.setForeground(Color.RED);
                break;
            case "orange":
                tvStatus.setText("⚠️ Warning: 80% of budget used!");
                tvStatus.setForeground(Color.ORANGE);
                break;
            default:
                tvStatus.setText("✅ Your budget is on track");
                tvStatus.setForeground(new Color(50, 205, 50));
                break;
        }
    }

    private void applyWhiteForeground(JLabel... labels) {
        for (JLabel l : labels) l.setForeground(Color.WHITE);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(212, 175, 55));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}

