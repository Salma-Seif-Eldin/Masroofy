package Views;

import javax.swing.*;
import java.awt.*;
import Controllers.BudgetManager;

/**
 * AuthActivity: نقطة الدخول للتطبيق.
 * تم التعديل لربط الـ PIN المكتشف بالـ BudgetManager.
 */
public class AuthActivity extends JPanel {
    
    public AuthActivity(BudgetManager manager, JFrame mainFrame) {
        // إعدادات التصميم (Navy & Gold Theme)
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 25, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // العنوان
        JLabel title = new JLabel("Welcome to Masroofy");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(212, 175, 55)); // ذهبي
        gbc.gridx = 0; gbc.gridy = 0;
        add(title, gbc);

        // أزرار الواجهة
        JButton signUpBtn = createStyledButton("Sign Up (Create PIN)");
        JButton signInBtn = createStyledButton("Sign In (Enter PIN)");

        gbc.gridy = 1;
        add(signUpBtn, gbc);

        gbc.gridy = 2;
        add(signInBtn, gbc);

        // --- المنطق (Actions) ---

        // 1. إنشاء حساب جديد
        signUpBtn.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(new PinSetupActivity(manager, mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        // 2. تسجيل الدخول باستخدام PIN موجود
        signInBtn.addActionListener(e -> {
            String savedPin = manager.getSavedPin(); // جلب الـ PIN من الإعدادات
            
            if (savedPin == null) {
                JOptionPane.showMessageDialog(this, 
                    "No account found. Please Sign Up first.", 
                    "Authentication Error", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                mainFrame.getContentPane().removeAll();
                // نمرر الـ manager والـ savedPin والـ Runnable للنجاح
                mainFrame.getContentPane().add(new PinActivity(manager, savedPin, () -> {
                    navigateToNext(mainFrame, manager);
                }));
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        });
    }

    /**
     * تحديد الوجهة التالية بناءً على وجود دورة ميزانية نشطة
     */
    private void navigateToNext(JFrame frame, BudgetManager manager) {
        frame.getContentPane().removeAll();
        
        // التحقق مما إذا كان المستخدم لديه دورة ميزانية مخزنة
        if (manager.getCurrentCycle() == null) {
            // إذا كان جديداً، نتوجه لإعداد الميزانية
            frame.getContentPane().add(new CycleSetupActivity(manager, frame));
        } else {
            // إذا كان لديه بيانات، نتوجه للداشبورد مباشرة
            frame.getContentPane().add(new DashboardActivity(manager));
        }
        
        frame.revalidate();
        frame.repaint();
    }

    /**
     * ميثود مساعدة لتنسيق الأزرار
     */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(new Color(212, 175, 55));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 45));
        return btn;
    }
}