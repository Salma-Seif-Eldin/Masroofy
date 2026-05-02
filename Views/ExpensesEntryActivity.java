/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Views;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Controllers.BudgetManager;
import Controllers.ExpenseController;

/**
 *
 * @author TOP 10
 */
public class ExpensesEntryActivity extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ExpensesEntryActivity.class.getName());

    private BudgetManager budgetManager;

    public ExpensesEntryActivity(BudgetManager manager) {
        this.budgetManager = manager;
        initComponents();
        populateCategories(); // Added from second file to ensure list is not empty
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ensure it triggers 'windowClosed'
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(203, 229, 255));
        setForeground(java.awt.Color.lightGray);

        jLabel1.setForeground(new java.awt.Color(0, 0, 102));
        jLabel1.setText("Amount");

        jLabel2.setForeground(new java.awt.Color(0, 0, 102));
        jLabel2.setText("Categeory");

        jButton1.setForeground(new java.awt.Color(0, 0, 102));
        jButton1.setText("Save Expense");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jComboBox1.setBackground(new java.awt.Color(227, 239, 252));
        jComboBox1.setForeground(new java.awt.Color(0, 0, 102));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jTextField1.setBackground(new java.awt.Color(227, 239, 252));
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setText("Notes");

        jTextField2.setBackground(new java.awt.Color(227, 239, 252));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(jTextField2))))
                .addContainerGap(109, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(jButton1)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        pack();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String amountText = jTextField1.getText().trim();
            
            // Added validation check from second file
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.");
                return;
            }

            double amount = Double.parseDouble(amountText);
            String note = jTextField2.getText().trim();
            String selectedCategory = jComboBox1.getSelectedItem().toString();
            
            int categoryId = 1;
            if (selectedCategory.equals("Food")) categoryId = 1;
            else if (selectedCategory.equals("Transport")) categoryId = 2;
            else if (selectedCategory.equals("Shopping")) categoryId = 3;
            else if (selectedCategory.equals("Health")) categoryId = 4;
            else if (selectedCategory.equals("Education")) categoryId = 5;
            else if (selectedCategory.equals("Entertainment")) categoryId = 6;

            ExpenseController controller = new ExpenseController(new Database.TransactionDAO(), budgetManager);
            boolean success = controller.processExpense(amount, categoryId, note);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Expense Saved Successfully!");
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error: Could not save expense.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateCategories() {
        jComboBox1.removeAllItems();
        jComboBox1.addItem("Food");
        jComboBox1.addItem("Transport");
        jComboBox1.addItem("Shopping");
        jComboBox1.addItem("Health");
        jComboBox1.addItem("Education");
        jComboBox1.addItem("Entertainment");
    }

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration
}
