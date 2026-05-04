package Views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import Models.Expense;
import Controllers.BudgetManager;
import Controllers.ExpenseController;

public class HistoryActivity extends JFrame {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private final BudgetManager budgetManager;
    private final ExpenseController expenseController;
    private JComboBox<String> categoryFilter;
    private JTextField startDateField;
    private JTextField endDateField;
    private DashboardActivity dashboard;

    public HistoryActivity(BudgetManager manager, DashboardActivity dashboard) {
        this.budgetManager = manager;
        this.dashboard = dashboard;
        this.expenseController = ExpenseController.createFor(budgetManager);
        
        setTitle("Expense History - User: " + budgetManager.getCurrentPin());
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Amount (EGP)", "Category", "Notes", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        expenseTable = new JTable(tableModel);
        setupUI();
        loadExpenses();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Transactions"));

        categoryFilter = new JComboBox<>(new String[]{"All", "Food", "Transport", "Shopping", "Health", "Education", "Entertainment", "Other"});
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        JButton btnApplyFilter = new JButton("Apply Filter");

        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryFilter);
        filterPanel.add(new JLabel("From (yyyy-mm-dd):"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDateField);
        filterPanel.add(btnApplyFilter);

        add(filterPanel, BorderLayout.NORTH);

        expenseTable.setFillsViewportHeight(true);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh All");

        btnDelete.setBackground(new Color(200, 50, 50));
        btnDelete.setForeground(Color.WHITE);
        btnEdit.setBackground(new Color(50, 150, 50));
        btnEdit.setForeground(Color.WHITE);

        btnApplyFilter.addActionListener(e -> applyFilterLogic());

        btnDelete.addActionListener(e -> {
            int row = expenseTable.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this expense?");
                if (confirm == JOptionPane.YES_OPTION) {
                    if (expenseController.modifyTransaction(id, "Delete", null)) {
                        loadExpenses();
                        JOptionPane.showMessageDialog(this, "Deleted successfully.");
                        dashboard.refreshUI();
                    }
                }
            }
        });
        
        btnEdit.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                double currentAmount = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
                String currentCategory = tableModel.getValueAt(selectedRow, 2).toString();
                String currentNotes = tableModel.getValueAt(selectedRow, 3).toString();

                JTextField amountField = new JTextField(String.valueOf(currentAmount));
                JTextField notesField = new JTextField(currentNotes);
                String[] categories = {"Food", "Transport", "Shopping", "Health", "Education", "Entertainment", "Other"};
                JComboBox<String> catBox = new JComboBox<>(categories);
                catBox.setSelectedItem(currentCategory);

                Object[] message = {
                    "New Amount (EGP):", amountField,
                    "Category:", catBox,
                    "Notes:", notesField
                };

                int option = JOptionPane.showConfirmDialog(this, message, "Edit Transaction", JOptionPane.OK_CANCEL_OPTION);
                
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        double newAmount = Double.parseDouble(amountField.getText());
                        int newCatId = catBox.getSelectedIndex() + 1;
                        String newNotes = notesField.getText();

                        Expense updatedData = new Expense(newAmount, newCatId, newNotes);

                        if (expenseController.modifyTransaction(id, "Edit", updatedData)) {
                            loadExpenses(); 
                            if (dashboard != null) {
                                dashboard.refreshUI(); 
                            }
                            JOptionPane.showMessageDialog(this, "Transaction updated and Dashboard refreshed!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to update transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        });

        btnRefresh.addActionListener(e -> loadExpenses());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyFilterLogic() {
        int catID = categoryFilter.getSelectedIndex();
        String start = startDateField.getText();
        String end = endDateField.getText();

        List<Expense> filtered = expenseController.filterHistory(catID, start, end);

        if (filtered != null && !filtered.isEmpty()) {
            updateTableData(filtered);
        } else {
            tableModel.setRowCount(0);
            JOptionPane.showMessageDialog(this, "No transactions found for these filters.");
        }
    }

    private void loadExpenses() {
        updateTableData(budgetManager.getExpenses());
    }

    private void updateTableData(List<Expense> expenses) {
        tableModel.setRowCount(0);
        if (expenses == null) return;

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

    private String getCategoryName(int id) {
        return Models.Category.getNameById(id);
    }
}