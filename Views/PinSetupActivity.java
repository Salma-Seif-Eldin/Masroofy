package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

/**
 * PinSetupActivity — Sign Up screen.
 *
 * Registers a brand-new PIN in the 'users' table.
 * If the PIN already exists, shows an error instead of overwriting.
 */
public class PinSetupActivity extends JPanel {

    private BudgetManager manager;
    private JFrame mainFrame;
    private JPasswordField pinField; // password field so digits are hidden
    private JButton saveBtn;

    /**
     * Constructs the PIN setup screen used for new user registration.
     *
     * @param manager   the {@link BudgetManager} used to register the new PIN
     * @param mainFrame the parent frame used for navigation
     */
    public PinSetupActivity(BudgetManager manager, JFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title
        JLabel titleLabel = new JLabel("Sign Up — Create PIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(212, 175, 55));
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // Instruction
        JLabel instrLabel = new JLabel("Choose a 4-digit PIN:", SwingConstants.CENTER);
        instrLabel.setForeground(Color.WHITE);
        instrLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridy = 1;
        add(instrLabel, gbc);

        // PIN field (masked)
        pinField = new JPasswordField(10);
        pinField.setFont(new Font("Arial", Font.BOLD, 22));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBackground(new Color(230, 230, 230));
        gbc.gridy = 2;
        add(pinField, gbc);

        // Register button
        saveBtn = new JButton("  Create Account");
        saveBtn.setBackground(new Color(212, 175, 55));
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 3;
        add(saveBtn, gbc);

        // Back button
        JButton backBtn = new JButton("← Back");
        backBtn.setBackground(new Color(60, 60, 80));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        add(backBtn, gbc);

        saveBtn.addActionListener(e -> handleSavePin());
        pinField.addActionListener(e -> handleSavePin()); // Enter key
        backBtn.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(new AuthActivity(manager, mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });
    }

    /**
     * Handles the save action for PIN setup, validating the entered PIN
     * and creating a new user account when valid.
     */
    private void handleSavePin() {
        String enteredPin = new String(pinField.getPassword()).trim();

        // Validate format
        if (enteredPin.length() != 4 || !enteredPin.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ PIN must be exactly 4 digits (numbers only).",
                    "Invalid PIN", JOptionPane.WARNING_MESSAGE);
            pinField.setText("");
            pinField.requestFocus();
            return;
        }

        // Check if already taken
        if (manager.pinExists(enteredPin)) {
            JOptionPane.showMessageDialog(this,
                    "<html>PIN <b>" + enteredPin + "</b> is already registered.<br>" +
                            "Please choose a different PIN, or go back and Sign In.</html>",
                    "PIN Already Exists", JOptionPane.WARNING_MESSAGE);
            pinField.setText("");
            pinField.requestFocus();
            return;
        }

        // Register the new PIN
        boolean registered = manager.registerPin(enteredPin);
        if (!registered) {
            JOptionPane.showMessageDialog(this,
                    "Database error while saving PIN. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Set as current user and go to cycle setup
        manager.setCurrentPin(enteredPin);
        manager.loadExistingBudget(); // will find no cycle yet — that's fine

        JOptionPane.showMessageDialog(this,
                "✅ Account created! Now set up your first budget cycle.",
                "Welcome!", JOptionPane.INFORMATION_MESSAGE);

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}
