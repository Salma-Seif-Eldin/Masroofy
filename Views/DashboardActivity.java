package Views;

import Controllers.BudgetManager;
import Controllers.ReportController;  
import Models.BudgetCycle;
import java.awt.*;
import javax.swing.*;

// Change: Extends JFrame instead of AppCompatActivity
public class DashboardActivity extends JPanel {

    // UI Elements: Desktop uses JLabel and JProgressBar
    private JLabel tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent, tvStatus;
    private JProgressBar pbBudgetProgress;
    
    // Controller
    private final BudgetManager budgetManager;
    private ReportController reportController;


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
        tvAllowance = new JLabel("Allowance: ");
        tvRemaining = new JLabel("Remaining: ");
        tvDailyLimit = new JLabel("Safe Daily Limit: ");
        tvDailySpent = new JLabel("Spent Today: ");
        tvStatus = new JLabel("Status: ");
        pbBudgetProgress = new JProgressBar(0, 100);
        pbBudgetProgress.setStringPainted(true);
        JButton btnReport = new JButton("Generate Report");
        btnReport.addActionListener(e -> {
        String reportData = reportController.generateSummaryReport();
        JOptionPane.showMessageDialog(this, reportData, "Cycle Summary", JOptionPane.INFORMATION_MESSAGE);
        });
        // Add them to the window
        add(btnReport);
        add(tvAllowance);
        add(tvRemaining);
        add(tvDailyLimit);
        add(tvDailySpent);
        add(pbBudgetProgress);
        add(tvStatus);
    }

    private void refreshUI() {
        BudgetCycle currentCycle = budgetManager.getCurrentCycle();

        if (currentCycle == null) {
            tvStatus.setText("No active cycle. Please start one!");
            return;
        }

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