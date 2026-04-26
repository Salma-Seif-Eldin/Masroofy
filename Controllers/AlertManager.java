package Controllers;
import java.util.Date;
import javax.swing.JOptionPane;

public class AlertManager {

    private Date lastAlertSent;
 

    private boolean permissionsGranted; //if user does not allow notifications
    

    public AlertManager() {
        this.lastAlertSent  = null;
        // No alerts sent yet on startup

        this.permissionsGranted = false;
        // Permissions not granted until requestPermissions() called
    }

    public boolean requestPermissions() {
        this.permissionsGranted = true;
        System.out.println("Notification permissions granted.");
        return true;
        
    }

    public void monitorBudgetTotalSpent(double totalSpent, double totalAllowance) {
        // Called by BudgetManager.addExpense() every time an expense is added
        // Checks if spending crossed a warning threshold

        if (!permissionsGranted) return;
        if (totalAllowance <= 0) return;

        double spentPercentage = (totalSpent / totalAllowance) * 100;
        // Calculate fresh percentage from current values

        if (spentPercentage >= 100.0) {
            triggerAlert(" Budget Exceeded", "You have spent your entire budget!");
        } else if (spentPercentage >= 80.0) {
            triggerAlert(" Budget Warning",
                "You have spent " + String.format("%.1f", spentPercentage) + "% of your budget!");
        }
        // Only one alert fires — 100% check first, then 80%
    }

    public void monitorDailyLimit(double todaySpent, double dailyLimit) {
        // Separate check for daily spending
        if (!permissionsGranted) return;
        if (dailyLimit <= 0) return;

        if (todaySpent > dailyLimit) {
            triggerAlert(" Daily Limit Exceeded",
                "You exceeded your daily limit of " + String.format("EGP %.2f", dailyLimit));
        }
    }

    public void triggerAlert(double percentageReached) {
        // Public version — called with just a percentage number
        if (shouldSuppressAlert()) return;

        String title   = " Budget Alert";
        String message = "You have reached "
                       + String.format("%.1f", percentageReached) + "% of your budget!";

        sendNotification(title, message);
        this.lastAlertSent = new Date();
        // Record that an alert was just sent
    }

    public void triggerAlert(String title, String message) {
        // Overloaded version — called with custom title and message
        if (shouldSuppressAlert()) return;

        sendNotification(title, message);
        this.lastAlertSent = new Date();
    }

    public void clearNotifications() {
        this.lastAlertSent = null;
        System.out.println("All notifications cleared.");
       
    }

    private boolean shouldSuppressAlert() {
        // Prevents spamming the user with repeated alerts
        if (lastAlertSent == null) return false;
        // No previous alert = don't suppress

        long now         = new Date().getTime();
        long lastSent    = lastAlertSent.getTime();
        long oneHourInMs = 60 * 60 * 1000;
      

        return (now - lastSent) < oneHourInMs;
        
    }

   private void sendNotification(String title, String message) {
    // Shows a real popup dialog instead of console print
    JOptionPane.showMessageDialog(
        null,
        message,
        title,
        JOptionPane.WARNING_MESSAGE
    );
}
}