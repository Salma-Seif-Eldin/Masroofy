package Controllers;

import java.util.Date;

/**
 * Manages budget alert notifications for the user.
 * <p>
 * Monitors both total cycle spending and daily spending against their respective
 * limits, triggering alerts when thresholds (80% and 100%) are reached.
 * Alerts are suppressed if one was already sent within the last hour to avoid spam.
 * Notifications are dispatched via an {@link AlertCallback} if registered,
 * or printed to the console as a fallback.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class AlertManager {

    private Date lastAlertSent;
    private boolean permissionsGranted;
    private AlertCallback callback;

    /**
     * Callback interface for receiving alert events.
     */
    public interface AlertCallback {
        /**
         * Called when a budget alert should be displayed to the user.
         *
         * @param title   the title of the alert
         * @param message the body message of the alert
         */
        void onAlert(String title, String message);
    }

    /**
     * Constructs a new AlertManager with permissions granted by default.
     */
    public AlertManager() {
        this.lastAlertSent = null;
        this.permissionsGranted = true;
    }

    /**
     * Registers the callback that will receive alert events.
     *
     * @param callback the {@link AlertCallback} to notify when an alert is triggered
     */
    public void setCallback(AlertCallback callback) {
        this.callback = callback;
    }

    /**
     * Grants notification permissions for this manager.
     *
     * @return {@code true} always, indicating permissions were granted
     */
    public boolean requestPermissions() {
        this.permissionsGranted = true;
        System.out.println("Notification permissions granted.");
        return true;
    }

    /**
     * Monitors total cycle spending and triggers an alert if 80% or 100%
     * of the total allowance has been spent.
     *
     * @param totalSpent     the total amount spent in the current cycle
     * @param totalAllowance the total budget allowance for the cycle
     */
    public void monitorBudgetTotalSpent(double totalSpent, double totalAllowance) {
        if (!permissionsGranted || totalAllowance <= 0) return;
        double spentPercentage = (totalSpent / totalAllowance) * 100;

        if (spentPercentage >= 100.0) {
            triggerAlert("Total Budget Reached", "Error: You have reached 100% of your total budget!");
        } else if (spentPercentage >= 80.0) {
            triggerAlert("Total Budget Warning", "Warning: You have spent more than 80% of your total budget.");
        }
    }

    /**
     * Monitors today's spending against the daily limit and triggers an alert
     * if 80% or 100% of the daily limit has been reached.
     *
     * @param todaySpent the total amount spent today
     * @param dailyLimit the recommended daily spending limit
     */
    public void monitorDailyLimit(double todaySpent, double dailyLimit) {
        if (!permissionsGranted || dailyLimit <= 0) return;
        double dailyPercentage = (todaySpent / dailyLimit) * 100;

        if (dailyPercentage >= 100.0) {
            triggerAlert("Daily Limit Exceeded", "Error: You have exceeded your daily limit, but you can still add more if you wish.");
        } else if (dailyPercentage >= 80.0) {
            triggerAlert("Daily Limit Warning", "Warning: You have spent more than 80% of your daily limit.");
        }
    }

    /**
     * Triggers an alert with a custom title and message if suppression conditions are not met.
     * Records the alert time to enforce the one-hour suppression window.
     *
     * @param title   the alert title
     * @param message the alert message body
     */
    public void triggerAlert(String title, String message) {
        if (shouldSuppressAlert()) return;
        sendNotification(title, message);
        this.lastAlertSent = new Date();
    }

    /**
     * Triggers a generic budget percentage alert if suppression conditions are not met.
     * Records the alert time to enforce the one-hour suppression window.
     *
     * @param percentageReached the percentage of the budget that has been reached
     */
    public void triggerAlert(double percentageReached) {
        if (shouldSuppressAlert()) return;
        String title = "Budget Alert";
        String message = "You have reached "
                       + String.format("%.1f", percentageReached) + "% of your budget!";
        sendNotification(title, message);
        this.lastAlertSent = new Date();
    }

    /**
     * Clears the last alert timestamp, allowing new alerts to be sent immediately.
     */
    public void clearNotifications() {
        this.lastAlertSent = null;
        System.out.println("All notifications cleared.");
    }

    /**
     * Determines whether an alert should be suppressed based on the one-hour cooldown window.
     *
     * @return {@code true} if an alert was sent within the last hour; {@code false} otherwise
     */
    private boolean shouldSuppressAlert() {
        if (lastAlertSent == null) return false;
        long now = new Date().getTime();
        long lastSent = lastAlertSent.getTime();
        long oneHourInMs = 60 * 60 * 1000;
        return (now - lastSent) < oneHourInMs;
    }

    /**
     * Dispatches the alert to the registered callback, or logs it to the console
     * if no callback is set.
     *
     * @param title   the alert title
     * @param message the alert message body
     */
    private void sendNotification(String title, String message) {
        if (callback != null) {
            callback.onAlert(title, message);
        } else {
            System.out.println("[ALERT] " + title + ": " + message);
        }
    }
}