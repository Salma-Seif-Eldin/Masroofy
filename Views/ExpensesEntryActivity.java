package Views;

import javax.swing.*;
import Controllers.BudgetManager;
import Controllers.ExpenseController;
import Controllers.ExpenseController.ExpenseResult;

/**
 * A dialog window that allows the user to enter and submit a new expense.
 * <p>
 * Displays fields for amount, category, and notes. On submission, delegates
 * validation and persistence to {@link ExpenseController}. Shows success or
 * error messages based on the result and closes automatically on success.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class ExpensesEntryActivity extends JFrame {

    private BudgetManager budgetManager;

    private JLabel jLabel1, jLabel2, jLabel3;
    private JButton jButton1;
    private JComboBox<String> jComboBox1;
    private JTextField jTextField1, jTextField2;

    /**
     * Constructs the ExpensesEntryActivity window and initializes all UI components.
     *
     * @param manager the {@link BudgetManager} used to process and store the expense
     */
    public ExpensesEntryActivity(BudgetManager manager) {
        this.budgetManager = manager;
        initComponents();
        populateCategories();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Initializes and lays out all UI components including labels, text fields,
     * category dropdown, and the save button.
     */
    private void initComponents() {
        jLabel1    = new JLabel("Amount");
        jLabel2    = new JLabel("Category");
        jLabel3    = new JLabel("Notes");
        jButton1   = new JButton("Save Expense");
        jComboBox1 = new JComboBox<>();
        jTextField1 = new JTextField(15);
        jTextField2 = new JTextField(15);

        setTitle("Add Expense");
        setSize(400, 320);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new java.awt.Color(203, 229, 255));

        jLabel1.setForeground(new java.awt.Color(0, 0, 102));
        jLabel2.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jButton1.setForeground(new java.awt.Color(0, 0, 102));

        jComboBox1.setBackground(new java.awt.Color(227, 239, 252));
        jComboBox1.setForeground(new java.awt.Color(0, 0, 102));
        jTextField1.setBackground(new java.awt.Color(227, 239, 252));
        jTextField2.setBackground(new java.awt.Color(227, 239, 252));

        jButton1.addActionListener(e -> jButton1ActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jButton1))
                .addGroup(layout.createParallelGroup()
                    .addComponent(jTextField1)
                    .addComponent(jComboBox1)
                    .addComponent(jTextField2))
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1).addComponent(jTextField1))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2).addComponent(jComboBox1))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3).addComponent(jTextField2))
                .addComponent(jButton1)
        );

        pack();
    }

    /**
     * Handles the Save Expense button action.
     * <p>
     * Validates user input, delegates expense processing to {@link ExpenseController},
     * and displays a success or error dialog based on the result.
     * Closes the window on success.
     * </p>
     */
    private void jButton1ActionPerformed() {
        try {
            String amountText = jTextField1.getText().trim();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
                return;
            }

            String note = jTextField2.getText().trim();
            String selectedCategory = (String) jComboBox1.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.");
                return;
            }

            int categoryId = getCategoryId(selectedCategory);
            ExpenseController controller = ExpenseController.createFor(budgetManager);
            ExpenseResult result = controller.processExpense(amount, categoryId, note);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + result.getMessage(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                if (result.hasWarning()) {
                    JOptionPane.showMessageDialog(this, result.getWarning(),
                        "Budget Warning", JOptionPane.WARNING_MESSAGE);
                }

                this.dispose();

            } else {
                int messageType = JOptionPane.ERROR_MESSAGE;
                String title = "Error";

                if ("daily_limit_exceeded".equals(result.getRejectionType())) {
                    messageType = JOptionPane.WARNING_MESSAGE;
                    title = "Daily Limit Exceeded";
                } else if ("budget_exceeded".equals(result.getRejectionType())) {
                    messageType = JOptionPane.ERROR_MESSAGE;
                    title = "Budget Exceeded";
                }

                JOptionPane.showMessageDialog(this,
                    "❌ " + result.getMessage(), title, messageType);

                jTextField1.setText("");
                jTextField1.requestFocus();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Populates the category dropdown with all predefined category names.
     */
    private void populateCategories() {
        jComboBox1.removeAllItems();
        for (String name : Models.Category.getAllNames()) {
            jComboBox1.addItem(name);
        }
    }

    /**
     * Converts a category name string to its corresponding numeric ID.
     *
     * @param name the category name to look up
     * @return the category ID corresponding to the given name
     */
    private int getCategoryId(String name) {
        return Models.Category.getIdByName(name);
    }
}