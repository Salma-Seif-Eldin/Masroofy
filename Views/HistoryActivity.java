package Views;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import Database.TransactionDAO;
import Models.Expense;
import Controllers.ExpenseController;
import java.util.List; 

public class HistoryActivity extends JFrame {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private TransactionDAO transactionDAO = new TransactionDAO();
    private ExpenseController expenseController = new ExpenseController(transactionDAO, null); // Pass the appropriate BudgetManager instance

    public HistoryActivity() {
        setTitle("Expense History");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Setup Table Columns
        String[] columnNames = {"ID", "Amount", "Category", "Date", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0);
        expenseTable = new JTable(tableModel);
        
        loadExpenses(); // Initial load

        // 2. Add UI Components
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnEdit = new JButton("Edit"); // Placeholder for now

        // 3. Delete Logic
        // Inside btnDelete action listener:
    btnDelete.addActionListener(e -> {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Use the Controller instead of the DAOs!
            // The Controller will handle both deleting the record and updating the balance.
            boolean success = expenseController.modifyTransaction(id, "Delete", null);

            if (success) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Deleted successfully.");
            }
        }
});

        // Layout
        setLayout(new BorderLayout());
        add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 4. Data Loading Logic
    private void loadExpenses() {
    tableModel.setRowCount(0);
    List<Expense> expenses = expenseController.loadHistory(0, 0, 0); // Controller returns a List
    
    if (expenses.isEmpty()) {
        // Display a message or show a specific label saying "No transactions yet"
        System.out.println("Displaying Empty State UI"); 
    } else {
        for (Expense exp : expenses) {
            tableModel.addRow(new Object[]{
                exp.getExpenseId(), exp.getAmount(), exp.getCategoryId(), exp.getTimestamp(), exp.getNotes()
            });
        }
    }
}
}