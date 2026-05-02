package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

public class PinSetupActivity extends JPanel {
    private BudgetManager manager;
    private JFrame mainFrame;
    private JTextField pinField;
    private JButton saveBtn;

    public PinSetupActivity(BudgetManager manager, JFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47)); 

        JLabel titleLabel = new JLabel("Sign Up - Set Security PIN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(212, 175, 55)); 

        pinField = new JTextField(10);
        pinField.setFont(new Font("Arial", Font.PLAIN, 18));
        pinField.setHorizontalAlignment(JTextField.CENTER);

        saveBtn = new JButton("Confirm & Register");
        saveBtn.setBackground(new Color(212, 175, 55));
        saveBtn.setForeground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0; add(titleLabel, gbc);
        gbc.gridy = 1; add(new JLabel("Choose a 4-digit PIN:"), gbc);
        gbc.gridy = 2; add(pinField, gbc);
        gbc.gridy = 3; add(saveBtn, gbc);

        saveBtn.addActionListener(e -> handleSavePin());
    }

    private void handleSavePin() {
        String enteredPin = pinField.getText().trim();

        if (enteredPin.length() == 4 && enteredPin.matches("\\d+")) {
            try {
                manager.savePin(enteredPin);
                // تحديث الـ Current PIN فوراً لضمان استمرارية الربط
                manager.setCurrentPin(enteredPin); 

                JOptionPane.showMessageDialog(this, "✅ Account Created!");
                
                mainFrame.getContentPane().removeAll();
                mainFrame.getContentPane().add(new CycleSetupActivity(manager, mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "⚠️ Invalid PIN. Must be 4 digits.");
        }
    }
}