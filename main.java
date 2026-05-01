import Controllers.BudgetManager;
import Database.DatabaseManager;
import Views.CycleSetupActivity;
import Views.DashboardActivity;
import javax.swing.*;

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
                frame.add(new DashboardActivity(budgetManager));
            }

            frame.setVisible(true);
        });
    }
}