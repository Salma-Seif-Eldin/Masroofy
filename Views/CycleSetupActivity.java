package Views;

import Controllers.BudgetManager;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class CycleSetupActivity extends JPanel {
    private BudgetManager budgetManager;
    private JFrame mainFrame;
    private JTextField allowanceField, startDateField, endDateField;
    private JLabel feedbackLabel;

    public CycleSetupActivity(BudgetManager budgetManager, JFrame mainFrame) {
        this.budgetManager = budgetManager;
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47));
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Start New Budget Cycle");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(212, 175, 55));
        gbc.gridwidth = 2; gbc.gridy = 0; add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; add(createLabel("Allowance:"), gbc);
        allowanceField = new JTextField(15); gbc.gridx = 1; add(allowanceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(createLabel("Start (yyyy-MM-dd):"), gbc);
        startDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 15);
        gbc.gridx = 1; add(startDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(createLabel("End (yyyy-MM-dd):"), gbc);
        endDateField = new JTextField(15); gbc.gridx = 1; add(endDateField, gbc);

        feedbackLabel = new JLabel(" ");
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 4; add(feedbackLabel, gbc);

        JButton saveBtn = new JButton("✅ Save Cycle");
        saveBtn.addActionListener(e -> saveCycle());
        gbc.gridy = 5; add(saveBtn, gbc);
    }

    private void saveCycle() {
        try {
            double allowance = Double.parseDouble(allowanceField.getText());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDateField.getText());
            Date end = sdf.parse(endDateField.getText());

            // المنيجر الآن يمتلك الـ PIN الصحيح بسبب التعديل في PinSetupActivity
            boolean success = budgetManager.startCycle(allowance, start, end);

            if (success) {
                feedbackLabel.setForeground(Color.GREEN);
                feedbackLabel.setText("✅ Saved! Redirecting...");
                new Timer(1000, e -> goDashboard()).start();
            } else {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText("❌ Error: Check dates or PIN.");
            }
        } catch (Exception ex) {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText("⚠️ Invalid input format.");
        }
    }

    private void goDashboard() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new DashboardActivity(budgetManager));
        mainFrame.revalidate(); mainFrame.repaint();
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE); return l;
    }
}