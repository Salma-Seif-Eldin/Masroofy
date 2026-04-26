import javax.swing.SwingUtilities;
import Database.DatabaseManager;

public class Main {

    public static void main(String[] args) {

        DatabaseManager.createTables();
     
        SwingUtilities.invokeLater(() -> {

            // ── Member 1 ──
            Controllers.BudgetManager budgetManager = new Controllers.BudgetManager();
            budgetManager.loadExistingBudget();
            // Loads saved cycle and expenses from database on startup

            // ── Temporary test window until all members finish ──
            javax.swing.JFrame frame = new javax.swing.JFrame("Masroofy — مصروفي");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Show CycleSetup if no active cycle
            // Show Dashboard if cycle already exists
            if (budgetManager.getCurrentCycle() == null) {
                System.out.println("No active cycle — open CycleSetup screen");
                // Member 4 will replace this with:
                // frame.add(new Views.CycleSetupPanel(budgetManager, frame));
            } else {
                System.out.println("Active cycle found — open Dashboard screen");
                // Member 4 will replace this with:
                // frame.add(new Views.DashboardPanel(budgetManager, frame));
            }

            frame.setVisible(true);
        });
    }
}