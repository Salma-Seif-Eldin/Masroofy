import Database.DatabaseManager;
import javax.swing.*;
import Controllers.BudgetManager;
import Views.CycleSetupActivity;

public class main {

    public static void main(String[] args) {

        // Step 1 — Create all database tables
        DatabaseManager.createTables();

        // framework Launch
        SwingUtilities.invokeLater(() -> {

            // Create shared BudgetManager
            BudgetManager budgetManager = new BudgetManager();
            budgetManager.loadExistingBudget();

            //  main window
            JFrame frame = new JFrame("Masroofy — مصروفي");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Show correct screen based on saved data
            if (budgetManager.getCurrentCycle() == null) {
                // No saved cycle → show setup form
                frame.add(new CycleSetupActivity(budgetManager, frame));
            } else {
                // Has saved cycle → placeholder for Member 4
                JLabel placeholder = new JLabel(
                    "Dashboard — Member 4 will implement this",
                    SwingConstants.CENTER
                );
                placeholder.setFont(new java.awt.Font("Arial",
                        java.awt.Font.BOLD, 18));
                frame.add(placeholder);
            }

            frame.setVisible(true);
        });
    }
}