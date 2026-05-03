package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

/**
 * AuthActivity — redesigned for multi-user support.
 *
 * SIGN UP flow:
 *   User enters a new 4-digit PIN → saved to 'users' table → CycleSetup → Dashboard
 *
 * SIGN IN flow:
 *   User types any previously registered PIN → if found → load that user's data → Dashboard
 *   If PIN not registered → show error, stay on Auth screen
 */
public class AuthActivity extends JPanel {

    public AuthActivity(BudgetManager manager, JFrame mainFrame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("Welcome to Masroofy", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(212, 175, 55));
        gbc.gridy = 0;
        add(title, gbc);

        // ── Subtitle ──────────────────────────────────────────────────────────
        JLabel subtitle = new JLabel("Smart Budget Tracker", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 180, 180));
        gbc.gridy = 1;
        add(subtitle, gbc);

        // ── Separator ─────────────────────────────────────────────────────────
        gbc.gridy = 2;
        add(Box.createVerticalStrut(10), gbc);

        // ── Sign Up button ────────────────────────────────────────────────────
        JButton signUpBtn = createStyledButton("🆕  Sign Up  (New User)");
        gbc.gridy = 3;
        add(signUpBtn, gbc);

        // ── Sign In button ────────────────────────────────────────────────────
        JButton signInBtn = createStyledButton("🔑  Sign In  (Existing User)");
        gbc.gridy = 4;
        add(signInBtn, gbc);

        // ── SIGN UP action ────────────────────────────────────────────────────
        signUpBtn.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(new PinSetupActivity(manager, mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        // ── SIGN IN action ────────────────────────────────────────────────────
        // User types the PIN they registered with. Any registered PIN works.
        signInBtn.addActionListener(e -> showSignInDialog(manager, mainFrame));
    }

    // =========================================================================
    // Sign-In dialog: user types their PIN directly
    // =========================================================================
    private void showSignInDialog(BudgetManager manager, JFrame mainFrame) {
        // Custom panel with a password field
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 25, 47));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("Enter your 4-digit PIN:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        g.gridy = 0; panel.add(lbl, g);

        JPasswordField pinField = new JPasswordField(10);
        pinField.setFont(new Font("Arial", Font.BOLD, 20));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        g.gridy = 1; panel.add(pinField, g);

        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Sign In",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String enteredPin = new String(pinField.getPassword()).trim();

        if (enteredPin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your PIN.", "Missing PIN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Check if this PIN is registered ──────────────────────────────────
        if (!manager.pinExists(enteredPin)) {
            // PIN not found → ask if they want to sign up instead
            int choice = JOptionPane.showConfirmDialog(this,
                "<html>PIN <b>" + enteredPin + "</b> is not registered.<br>" +
                "Would you like to Sign Up with this PIN?</html>",
                "PIN Not Found",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                // Register and go to CycleSetup
                manager.registerPin(enteredPin);
                manager.setCurrentPin(enteredPin);
                manager.loadExistingBudget();
                mainFrame.getContentPane().removeAll();
                mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();
            }
            // else: stay on Auth screen
            return;
        }

        // ── PIN found → load that user's data and navigate ───────────────────
        manager.setCurrentPin(enteredPin);
        manager.loadExistingBudget();

        mainFrame.getContentPane().removeAll();
        if (manager.getCurrentCycle() == null) {
            // Registered but never set up a cycle yet
            mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
        } else {
            // Has existing data → go straight to Dashboard
            mainFrame.getContentPane().add(new DashboardActivity(manager));
        }
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setBackground(new Color(212, 175, 55));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(260, 48));
        return btn;
    }
}
