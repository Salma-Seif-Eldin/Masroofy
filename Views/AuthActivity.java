package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

/**
 * The authentication screen of the Masroofy application.
 * Supports two flows:
 * <ul>
 * <li><b>Sign Up:</b> User enters a new 4-digit PIN → saved to the database
 * → navigates to {@link CycleSetupActivity} → {@link DashboardActivity}</li>
 * <li><b>Sign In:</b> User enters an existing PIN → if found, loads that user's
 * data and navigates to the dashboard; if not found, offers to register
 * instead</li>
 * </ul>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class AuthActivity extends JPanel {

    /**
     * Constructs the AuthActivity panel with Sign Up and Sign In buttons.
     *
     * @param manager   the {@link BudgetManager} handling user authentication and
     *                  data
     * @param mainFrame the parent {@link JFrame} used for screen navigation
     */
    public AuthActivity(BudgetManager manager, JFrame mainFrame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Welcome to Masroofy", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(212, 175, 55));
        gbc.gridy = 0;
        add(title, gbc);

        JLabel subtitle = new JLabel("Smart Budget Tracker", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 180, 180));
        gbc.gridy = 1;
        add(subtitle, gbc);

        gbc.gridy = 2;
        add(Box.createVerticalStrut(10), gbc);

        JButton signUpBtn = createStyledButton("🆕  Sign Up  (New User)");
        gbc.gridy = 3;
        add(signUpBtn, gbc);

        JButton signInBtn = createStyledButton("🔒  Sign In  (Existing User)");
        gbc.gridy = 4;
        add(signInBtn, gbc);

        signUpBtn.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(new PinSetupActivity(manager, mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        signInBtn.addActionListener(e -> showSignInDialog(manager, mainFrame));
    }

    /**
     * Displays a dialog prompting the user to enter their registered PIN.
     * <p>
     * If the PIN exists in the database, the user's data is loaded and they are
     * navigated to either the dashboard or the cycle setup screen. If the PIN is
     * not found, the user is offered the option to register it.
     * </p>
     *
     * @param manager   the {@link BudgetManager} used to verify and load user data
     * @param mainFrame the parent {@link JFrame} used for navigation
     */
    private void showSignInDialog(BudgetManager manager, JFrame mainFrame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 25, 47));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("Enter your 4-digit PIN:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        g.gridy = 0;
        panel.add(lbl, g);

        JPasswordField pinField = new JPasswordField(10);
        pinField.setFont(new Font("Arial", Font.BOLD, 20));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        g.gridy = 1;
        panel.add(pinField, g);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Sign In",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION)
            return;

        String enteredPin = new String(pinField.getPassword()).trim();

        if (enteredPin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your PIN.", "Missing PIN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!manager.pinExists(enteredPin)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "<html>PIN <b>" + enteredPin + "</b> is not registered.<br>" +
                            "Would you like to Sign Up with this PIN?</html>",
                    "PIN Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                manager.registerPin(enteredPin);
                manager.setCurrentPin(enteredPin);
                manager.loadExistingBudget();
                mainFrame.getContentPane().removeAll();
                mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();
            }
            return;
        }

        manager.setCurrentPin(enteredPin);
        manager.loadExistingBudget();

        mainFrame.getContentPane().removeAll();
        if (manager.getCurrentCycle() == null) {
            mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
        } else {
            mainFrame.getContentPane().add(new DashboardActivity(manager));
        }
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Creates a consistently styled button used on the auth screen.
     *
     * @param text the label text to display on the button
     * @return a styled {@link JButton}
     */
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