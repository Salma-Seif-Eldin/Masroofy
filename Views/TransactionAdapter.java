package Views;

import javax.swing.table.AbstractTableModel;
import Models.Expense;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * This class adapts a List of Expense objects for a JTable.
 * It handles how the data is mapped to columns and rows.
 */
public class TransactionAdapter extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Amount", "Category", "Date", "Notes"};
    private List<Expense> expenseList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public TransactionAdapter(List<Expense> expenses) {
        this.expenseList = expenses;
    }

    // Update the list and refresh the table (notifyDataSetChanged)
    public void setExpenses(List<Expense> expenses) {
        this.expenseList = expenses;
        fireTableDataChanged(); 
    }

    @Override
    public int getRowCount() {
        return expenseList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expense expense = expenseList.get(rowIndex);
        switch (columnIndex) {
            case 0: return expense.getExpenseId();
            case 1: return String.format("%.2f EGP", expense.getAmount());
            case 2: return expense.getCategoryId();
            case 3: return dateFormat.format(expense.getTimestamp());
            case 4: return expense.getNotes();
            default: return null;
        }
    }
}
