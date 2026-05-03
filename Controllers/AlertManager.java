package Controllers;

import java.util.Date;

public class AlertManager {
    private Date lastAlertSent;
    private boolean permissionsGranted;
    private AlertCallback callback;

    // NEW: Interface to decouple from Swing
    public interface AlertCallback {
        void onAlert(String title, String message);
    }

    public void setCallback(AlertCallback callback) {
        this.callback = callback;
    }

    public AlertManager() {
        this.lastAlertSent = null;
        this.permissionsGranted = false;
    }

    public boolean requestPermissions() {
        this.permissionsGranted = true;
        System.out.println("Notification permissions granted.");
        return true;
    }

    public void monitorBudgetTotalSpent(double totalSpent, double totalAllowance) {
        if (!permissionsGranted) return;
        if (totalAllowance <= 0) return;
        double spentPercentage = (totalSpent / totalAllowance) * 100;
        if (spentPercentage >= 100.0) {
            triggerAlert("Budget Exceeded", "You have spent your entire budget!");
        } else if (spentPercentage >= 80.0) {
            triggerAlert("Budget Warning",
                "You have spent " + String.format("%.1f", spentPercentage) + "% of your budget!");
        }
    }

    public void monitorDailyLimit(double todaySpent, double dailyLimit) {
        if (!permissionsGranted) return;
        if (dailyLimit <= 0) return;
        if (todaySpent > dailyLimit) {
            triggerAlert("Daily Limit Exceeded",
                "You exceeded your daily limit of " + String.format("EGP %.2f", dailyLimit));
        }
    }

    public void triggerAlert(double percentageReached) {
        if (shouldSuppressAlert()) return;
        String title = "Budget Alert";
        String message = "You have reached "
                       + String.format("%.1f", percentageReached) + "% of your budget!";
        sendNotification(title, message);
        this.lastAlertSent = new Date();
    }

    public void triggerAlert(String title, String message) {
        if (shouldSuppressAlert()) return;
        sendNotification(title, message);
        this.lastAlertSent = new Date();
    }

    public void clearNotifications() {
        this.lastAlertSent = null;
        System.out.println("All notifications cleared.");
    }

    private boolean shouldSuppressAlert() {
        if (lastAlertSent == null) return false;
        long now = new Date().getTime();
        long lastSent = lastAlertSent.getTime();
        long oneHourInMs = 60 * 60 * 1000;
        return (now - lastSent) < oneHourInMs;
    }

    // FIXED: Delegate to callback instead of direct JOptionPane
    private void sendNotification(String title, String message) {
        if (callback != null) {
            callback.onAlert(title, message);
        } else {
            System.out.println("[ALERT] " + title + ": " + message);
        }
    }
}