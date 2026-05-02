package Views;

import Controllers.BudgetManager;
import Controllers.ReportController;  
import Models.BudgetCycle;
import Models.DashboardModel; // استيراد الموديل
import java.awt.*;
import javax.swing.*;

public class DashboardActivity extends JPanel {
    
    private JLabel tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent, tvStatus;
    private JProgressBar pbBudgetProgress;
    private final BudgetManager budgetManager;
    private ReportController reportController;

    public DashboardActivity(BudgetManager manager) {
        this.budgetManager = manager;
        this.reportController = new ReportController(budgetManager);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(10, 25, 47));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initViews(); // استدعاء تهيئة العناصر
        refreshUI(); // استدعاء تحديث البيانات
    }

    private void initViews() {
        // 1. تهيئة الـ Labels
        tvStatus = new JLabel("Checking budget status...");
        tvAllowance = new JLabel("Total Allowance: 0.00");
        tvRemaining = new JLabel("Remaining: 0.00");
        tvDailyLimit = new JLabel("Safe Daily Limit: 0.00");
        tvDailySpent = new JLabel("Spent Today: 0.00");
        
        applyWhiteForeground(tvStatus, tvAllowance, tvRemaining, tvDailyLimit, tvDailySpent);

        // 2. تهيئة الـ ProgressBar
        pbBudgetProgress = new JProgressBar(0, 100);
        pbBudgetProgress.setStringPainted(true);
        pbBudgetProgress.setForeground(new Color(212, 175, 55)); // لون ذهبي للتحميل

        // 3. تهيئة الأزرار
        JButton btnAddExpense = new JButton("Add Expense");
        JButton btnHistory = new JButton("View History");
        JButton btnReport = new JButton("Generate Report");

        // 4. إعداد الـ Actions
        btnAddExpense.addActionListener(e -> {
            ExpensesEntryActivity entry = new ExpensesEntryActivity(budgetManager);
            entry.setVisible(true);
            entry.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    budgetManager.loadExistingBudget();
                    refreshUI(); // إعادة التحديث بالاعتماد على الموديل
                }
            });
        });

        btnHistory.addActionListener(e -> new HistoryActivity(budgetManager).setVisible(true));

        btnReport.addActionListener(e -> {
            String report = reportController.generateSummaryReport();
            JOptionPane.showMessageDialog(this, report, "Report Summary", JOptionPane.INFORMATION_MESSAGE);
        });

        // 5. إضافة العناصر للـ Panel بترتيب منظم
        add(tvStatus);
        add(Box.createVerticalStrut(10));
        add(tvAllowance);
        add(tvRemaining);
        add(Box.createVerticalStrut(15));
        add(pbBudgetProgress);
        add(Box.createVerticalStrut(15));
        add(tvDailyLimit);
        add(tvDailySpent);
        add(Box.createVerticalStrut(20));
        add(btnAddExpense);
        add(Box.createVerticalStrut(10));
        add(btnHistory);
        add(Box.createVerticalStrut(10));
        add(btnReport);
    }

    private void refreshUI() {
        // الربط مع الـ Model: نطلب من المنيجر تزويدنا بكائن DashboardModel كامل البيانات
        DashboardModel uiModel = budgetManager.getDashboardData();
        
        if (uiModel == null) {
            tvStatus.setText("No active cycle found.");
            return;
        }

        // تحديث النصوص مباشرة من الـ Model (بدون أي حسابات منطقية هنا)
        tvAllowance.setText(String.format("Total Allowance: %.2f EGP", uiModel.getTotalAllowance()));
        tvRemaining.setText(String.format("Remaining: %.2f EGP", uiModel.getRemainingBudget()));
        tvDailyLimit.setText(String.format("Safe Daily Limit: %.2f EGP", uiModel.getSafeDailyLimit()));
        tvDailySpent.setText(String.format("Spent Today: %.2f EGP", uiModel.getTotalSpent()));

        // تحديث الـ ProgressBar
        double spentPercent = uiModel.getSpendingPercentage();
        pbBudgetProgress.setValue((int) spentPercent);
        
        // تحديث لون الحالة بناءً على ما حدده الـ Model (Green, Orange, Red)
        updateStatusColor(uiModel.getStatusColor());
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
                tvStatus.setForeground(new Color(50, 205, 50)); // Lime Green
                break;
        }
    }

    private void applyWhiteForeground(JLabel... labels) {
        for (JLabel l : labels) l.setForeground(Color.WHITE);
    }
}