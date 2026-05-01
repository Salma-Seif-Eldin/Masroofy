
import javax.swing.*;
import Database.DatabaseManager;
import Database.TransactionDAO;
import Database.CycleDAO;
import Controllers.BudgetManager;
import Controllers.ExpenseController;
import Views.DashboardActivity;
import Views.CycleSetupActivity;

public class main {
    public static void main(String[] args) {
        // 1. Initialize Database Tables
        // This ensures your SQLite file and tables exist before anything else runs.
        DatabaseManager.createTables();

        // 2. Initialize Shared Data Access Objects (DAOs)
        CycleDAO cycleDAO = new CycleDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        // 3. Initialize Shared Controllers (The Glue)
        // We create one instance of BudgetManager so data stays synced across screens.
        BudgetManager budgetManager = new BudgetManager();
        budgetManager.loadExistingBudget(); // Checks if a cycle exists in the DB

        // 4. Create the Main Window (JFrame)
        JFrame mainFrame = new JFrame("Masroofy - Smart Budget Tracker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 500);
        mainFrame.setLocationRelativeTo(null);

        // 5. App Entry Logic: Setup or Dashboard?
        SwingUtilities.invokeLater(() -> {
            if (budgetManager.getCurrentCycle() == null) {
                // If no budget is found, go to the Setup Panel
                mainFrame.getContentPane().add(new CycleSetupActivity(budgetManager, mainFrame));
            } else {
                // If budget exists, go straight to the Dashboard
                mainFrame.getContentPane().add(new DashboardActivity(budgetManager));
            }
            
            mainFrame.setVisible(true);
        });
    }
}