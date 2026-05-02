package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

/**
 * صفحة إدخال الـ PIN (Sign In).
 * تم التعديل لربط الحساب الحالي بالـ BudgetManager قبل الدخول.
 */
public class PinActivity extends JPanel {
    private JPasswordField pinField;
    private JButton loginBtn;
    private String correctPin;
    private BudgetManager manager;
    private Runnable onSuccess;

    public PinActivity(BudgetManager manager, String correctPin, Runnable onSuccess) {
        this.manager = manager;
        this.correctPin = correctPin;
        this.onSuccess = onSuccess;
        
        // إعدادات التصميم (Glassmorphism inspired)
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47)); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; 

        // العنوان
        JLabel label = new JLabel("Enter PIN to Access Masroofy");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(new Color(212, 175, 55)); // ذهبي
        gbc.gridy = 0;
        add(label, gbc);

        // حقل إدخال الـ PIN
        pinField = new JPasswordField(10);
        pinField.setFont(new Font("Arial", Font.BOLD, 22));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBackground(new Color(230, 230, 230));
        gbc.gridy = 1;
        add(pinField, gbc);

        // زر فتح التطبيق
        loginBtn = new JButton("Unlock Account");
        loginBtn.setBackground(new Color(212, 175, 55));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 2;
        add(loginBtn, gbc);

        // منطق البرمجة عند الضغط على الزر
        loginBtn.addActionListener(e -> handleLogin());
        
        // السماح بالدخول عند الضغط على Enter في الكيبورد
        pinField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String enteredPin = new String(pinField.getPassword());
        
        if (enteredPin.equals(correctPin)) {
            // الخطوة الأهم لتعدد المستخدمين:
            manager.setCurrentPin(enteredPin); // تعيين المستخدم الحالي في النظام
            manager.loadExistingBudget();      // تحميل بياناته هو فقط من الداتا بيز
            
            onSuccess.run(); // الانتقال للـ Dashboard أو الإعداد
        } else {
            JOptionPane.showMessageDialog(this, 
                "Incorrect PIN! Please try again.", 
                "Access Denied", 
                JOptionPane.ERROR_MESSAGE);
            pinField.setText("");
            pinField.requestFocus();
        }
    }
}