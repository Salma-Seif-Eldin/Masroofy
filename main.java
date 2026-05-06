import javax.swing.*;
import Database.DatabaseManager;
import Controllers.BudgetManager;
import Views.AuthActivity;
import Views.DashboardActivity;
import Views.CycleSetupActivity;

/**
 * Entry point for the Masroofy budgeting application.
 * <p>
 * Initializes the database schema and shows the login/sign-up flow.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class main {

    /**
     * Constructs the main application launcher.
     */
    public main() {
    }

    /**
     * Launches the Masroofy budgeting application and initializes the database.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        DatabaseManager.createTables();
        BudgetManager budgetManager = new BudgetManager();

        JFrame mainFrame = new JFrame("Masroofy - Smart Budget Tracker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(450, 620);
        mainFrame.setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            mainFrame.getContentPane().add(new AuthActivity(budgetManager, mainFrame));
            mainFrame.setVisible(true);
        });
    }

    /**
     * Navigates the user to the appropriate next screen based on whether a
     * budget cycle is already active.
     *
     * @param frame   the main application frame whose content pane is replaced
     * @param manager the {@link BudgetManager} containing the current budget state
     */
    public static void navigateToApplicationFlow(JFrame frame, BudgetManager manager) {
        frame.getContentPane().removeAll();
        if (manager.getCurrentCycle() == null) {
            frame.getContentPane().add(new CycleSetupActivity(manager, frame));
        } else {
            frame.getContentPane().add(new DashboardActivity(manager));
        }
        frame.revalidate();
        frame.repaint();
    }
}
