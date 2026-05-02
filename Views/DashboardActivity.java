package Views;

import Controllers.BudgetManager;
import Controllers.ReportController;  
import Models.BudgetCycle;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

// Change: Extends JFrame instead of AppCompatActivity
public class DashboardActivity extends JPanel {

    // UI Elements: Desktop uses JLabel and JProgressBar
    private JLabel tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent, tvStatus;
    private JProgressBar pbBudgetProgress;
    
    // Controller
    private final BudgetManager budgetManager;
    private ReportController reportController;
        private SpendingChart spendingChart;



    public DashboardActivity(BudgetManager manager) {
    this.budgetManager = manager;
    this.reportController = new ReportController(budgetManager);

    // 1. Set the layout for THIS panel
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // 2. Initialize and refresh
    initViews();
    refreshUI();
}

    private void initViews() {
    // 1. Set Layout and Padding
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // 2. Initialize the labels you already have
    tvStatus = new JLabel("Status: Loading...");
    tvAllowance = new JLabel("Total Allowance: 0.00");
    tvRemaining = new JLabel("Remaining: 0.00");
    tvDailyLimit = new JLabel("Daily Limit: 0.00");
    tvDailySpent = new JLabel("Spent Today: 0.00");
    
    pbBudgetProgress = new JProgressBar(0, 100);
    pbBudgetProgress.setStringPainted(true);

    // 3. Navigation Buttons
    JButton btnAddExpense = new JButton("Add Expense");
    JButton btnHistory = new JButton("View History");
    JButton btnReport = new JButton("Generate Report");

    // 4. Button Logic
    btnAddExpense.addActionListener(e -> {
        // FIX: Pass the budgetManager here!
        ExpensesEntryActivity entry = new ExpensesEntryActivity(budgetManager);
        entry.setVisible(true);
        
        // Refresh when closed
        entry.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshUI();
            }
        });
    });

    btnHistory.addActionListener(e -> new HistoryActivity().setVisible(true));

    btnReport.addActionListener(e -> {
        String report = reportController.generateSummaryReport();
        JOptionPane.showMessageDialog(this, report);
    });

    // 5. Add everything to the panel
    add(tvStatus);
    add(Box.createVerticalStrut(10));
    add(tvAllowance);
    add(tvRemaining);
    add(Box.createVerticalStrut(10));
    add(pbBudgetProgress);
    add(Box.createVerticalStrut(10));
    add(tvDailyLimit);
    add(tvDailySpent);
    add(Box.createVerticalStrut(20));
    add(btnAddExpense);
    add(btnHistory);
    add(btnReport);
}
    private void refreshUI() {
        BudgetCycle currentCycle = budgetManager.getCurrentCycle();

        if (currentCycle == null) {
            tvStatus.setText("No active cycle. Please start one!");
            return;
        }
        Map<String, Double> categoryData = reportController.getSpendingByCategory();
        spendingChart.setData(categoryData);

        // Get calculations from BudgetManager
        double remaining = budgetManager.getRemainingBudget();
        double dailyLimit = budgetManager.getDailyLimit();
        double dailySpent = budgetManager.getDailySpent();
        double spentPercent = budgetManager.getSpentPercentage();

        // Update Text Fields
        tvAllowance.setText(String.format("Total Allowance: %.2f EGP", currentCycle.getTotalAllowance()));
        tvRemaining.setText(String.format("Remaining: %.2f EGP", remaining));
        tvDailyLimit.setText(String.format("Safe Daily Limit: %.2f EGP", dailyLimit));
        tvDailySpent.setText(String.format("Spent Today: %.2f EGP", dailySpent));

        // Update Progress Bar
        pbBudgetProgress.setValue((int) spentPercent);

        // Visual Warning (Desktop uses Foreground Color)
        if (currentCycle.isEightyPercentReached()) {
            tvStatus.setText("Warning: 80% of budget used!");
            tvStatus.setForeground(Color.RED);
        } else {
            tvStatus.setText("Your budget is on track");
            tvStatus.setForeground(new Color(0, 128, 0)); // Dark Green
        }
        
        // Daily Limit Alert (Desktop uses JOptionPane instead of Toast)
        if (dailySpent > dailyLimit) {
            JOptionPane.showMessageDialog(this, "You have exceeded your daily limit!", "Budget Alert", JOptionPane.WARNING_MESSAGE);
        }
    }
}
