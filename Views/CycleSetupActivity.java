package Views;

import Controllers.BudgetManager;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class CycleSetupActivity extends JPanel {

 

    private BudgetManager budgetManager;
    // controllers obj that handles all logic and database saving

    private JFrame mainFrame;
    // main window 

    private JTextField allowanceField;
    // allowance input box

    private JTextField startDateField;
    // start date input box
    private JTextField endDateField;
    // end date input box

    private JLabel feedbackLabel;
    // msg if success or error

   
    public CycleSetupActivity(BudgetManager budgetManager, JFrame mainFrame) {
        this.budgetManager = budgetManager;
        this.mainFrame     = mainFrame;

        setLayout(new GridBagLayout());
        

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        // 10px padding around every component

        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Components stretch horizontally to fill their cell

        buildUI(gbc);
        // Build all the form components
    }
//ui methods

    private void buildUI(GridBagConstraints gbc) {

        // ── Title ──
        JLabel title = new JLabel(" Start New Budget Cycle");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.gridwidth = 2;
        // Span across 2 columns so title is centered
        add(title, gbc);

        // ── Allowance Row ──
        gbc.gridwidth = 1;
        // Back to 1 column per component
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Total Allowance (EGP):"), gbc);
        // Label on the left

        allowanceField = new JTextField(15);
        gbc.gridx = 1;
        add(allowanceField, gbc);
        // Input box on the right

        // ── Start Date Row ──
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Start Date (yyyy-MM-dd):"), gbc);

        startDateField = new JTextField(15);
        gbc.gridx = 1;
        add(startDateField, gbc);

        // ── End Date Row ──
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("End Date (yyyy-MM-dd):"), gbc);

        endDateField = new JTextField(15);
        gbc.gridx = 1;
        add(endDateField, gbc);

        // ── Feedback Label ──
        feedbackLabel = new JLabel(" ");
        // Starts empty — fills with message after user submits
        feedbackLabel.setForeground(Color.RED);
        // Default color red for errors
        gbc.gridx     = 0;
        gbc.gridy     = 4;
        gbc.gridwidth = 2;
        add(feedbackLabel, gbc);

        // ── Buttons Row ──
        JPanel btnPanel = new JPanel(new FlowLayout());
        // FlowLayout puts buttons side by side

        JButton saveBtn = new JButton("✅ Save Cycle");
        JButton backBtn = new JButton("⬅ Back");

        saveBtn.addActionListener(e -> saveCycle());
        // When Save clicked → call saveCycle() method

        backBtn.addActionListener(e -> goBack());
        // When Back clicked → go back to dashboard

        btnPanel.add(saveBtn);
        btnPanel.add(backBtn);

        gbc.gridy = 5;
        add(btnPanel, gbc);
    }

   //save cycle logic 
    private void saveCycle() {
        // Called when user clicks the Save Cycle button
        // Reads form, validates, calls BudgetManager

        try {
            // ── Read and validate allowance ──
            String allowanceText = allowanceField.getText().trim();
            if (allowanceText.isEmpty()) {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText(" Please enter an allowance amount.");
                return;
            }

            double allowance = Double.parseDouble(allowanceText);
            // Throws NumberFormatException if not a valid number

            // ── Read and validate dates ──
            String startText = startDateField.getText().trim();
            String endText   = endDateField.getText().trim();

            if (startText.isEmpty() || endText.isEmpty()) {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText("Please enter both start and end dates.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            // Strict parsing — rejects invalid dates like 2024-13-45

            Date start = sdf.parse(startText);
            Date end   = sdf.parse(endText);
            // Throws ParseException if format is wrong

            // ── Call BudgetManager ──
            boolean success = budgetManager.startCycle(allowance, start, end);
            // BudgetManager validates dates and saves to database

            if (success) {
                feedbackLabel.setForeground(Color.GREEN);
                feedbackLabel.setText("✅ Budget cycle saved successfully!");

                // Wait 1.5 seconds then go to dashboard
                Timer timer = new Timer(1500, e -> goBack());
                timer.setRepeats(false);
                // Only fires once
                timer.start();

            } else {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText(" Invalid data. Make sure end date is after start date.");
            }

        } catch (NumberFormatException ex) {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText(" Allowance must be a number. Example: 5000");

        } catch (Exception ex) {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText(" Date format must be yyyy-MM-dd. Example: 2024-03-01");
        }
    }

    //navigation  

    private void goBack() {
    mainFrame.getContentPane().removeAll();
    
    mainFrame.getContentPane().add(new DashboardActivity(budgetManager));
    
    mainFrame.revalidate();
    mainFrame.repaint();
}
}