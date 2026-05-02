package Views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import Models.Expense;
import Controllers.BudgetManager;
import Controllers.ExpenseController;
import Database.TransactionDAO;


public class HistoryActivity extends JFrame {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private final BudgetManager budgetManager;
    private final ExpenseController expenseController;

    public HistoryActivity(BudgetManager manager) {
        this.budgetManager = manager;
        this.expenseController = new ExpenseController(new TransactionDAO(), budgetManager);

        setTitle("Expense History - User: " + budgetManager.getCurrentPin());
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Amount (EGP)", "Category", "Notes", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        expenseTable = new JTable(tableModel);
        setupUI();
        loadExpenses();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // تخصيص شكل الجدول
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        // أزرار التحكم
        JPanel buttonPanel = new JPanel();
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");

        btnDelete.setBackground(new Color(200, 50, 50));
        btnDelete.setForeground(Color.WHITE);

        // منطق الحذف
        btnDelete.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?");
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (expenseController.modifyTransaction(id, "Delete", null)) {
                        tableModel.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(this, "Expense deleted successfully.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            }
        });

        btnRefresh.addActionListener(e -> loadExpenses());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnDelete);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadExpenses() {
        tableModel.setRowCount(0);
        // جلب البيانات المفلترة للمستخدم الحالي من الـ Manager
        List<Expense> expenses = budgetManager.getExpenses();
        
        if (expenses == null || expenses.isEmpty()) {
            System.out.println("No history found for user: " + budgetManager.getCurrentPin());
            return;
        }

        for (Expense exp : expenses) {
            tableModel.addRow(new Object[]{
                exp.getExpenseId(),
                String.format("%.2f", exp.getAmount()),
                getCategoryName(exp.getCategoryId()),
                exp.getNotes(),
                exp.getTimestamp().toString()
            });
        }
    }

    // ميثود مساعدة لتحويل الـ ID لاسم نصي
    private String getCategoryName(int id) {
        return switch (id) {
            case 1 -> "Food";
            case 2 -> "Transport";
            case 3 -> "Shopping";
            case 4 -> "Health";
            case 5 -> "Education";
            case 6 -> "Entertainment";
            default -> "Other";
        };
    }
}