import javax.swing.*;
import Database.DatabaseManager;
import Controllers.BudgetManager;
import Views.AuthActivity;
import Views.DashboardActivity;
import Views.CycleSetupActivity;

public class main {

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
