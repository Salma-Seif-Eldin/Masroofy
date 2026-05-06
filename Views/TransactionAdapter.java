package Views;

import javax.swing.table.AbstractTableModel;
import Models.Expense;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * A Swing {@link javax.swing.table.TableModel} adapter for displaying a list of
 * {@link Expense} objects in a {@link javax.swing.JTable}.
 * <p>
 * Maps expense fields to five columns: ID, Amount, Category, Date, and Notes.
 * Supports dynamic data updates via {@link #setExpenses(List)}.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class TransactionAdapter extends AbstractTableModel {
    private final String[] columnNames = { "ID", "Amount", "Category", "Date", "Notes" };
    private List<Expense> expenseList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Constructs a TransactionAdapter with the given list of expenses.
     *
     * @param expenses the initial list of {@link Expense} objects to display
     */
    public TransactionAdapter(List<Expense> expenses) {
        this.expenseList = expenses;
    }

    /**
     * Updates the displayed expense list and notifies the table to refresh.
     *
     * @param expenses the new list of {@link Expense} objects to display
     */
    public void setExpenses(List<Expense> expenses) {
        this.expenseList = expenses;
        fireTableDataChanged();
    }

    /**
     * Returns the number of rows in the table (one per expense).
     *
     * @return the number of expenses in the list
     */
    @Override
    public int getRowCount() {
        return expenseList.size();
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the total number of columns (5)
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the display name of the specified column.
     *
     * @param col the column index (0-based)
     * @return the column header name
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Returns the value to display in a specific table cell.
     * Column mapping:
     * <ul>
     * <li>0 – Expense ID</li>
     * <li>1 – Amount (formatted with EGP)</li>
     * <li>2 – Category ID</li>
     * <li>3 – Timestamp (formatted as yyyy-MM-dd HH:mm)</li>
     * <li>4 – Notes</li>
     * </ul>
     *
     * @param rowIndex    the row index of the cell
     * @param columnIndex the column index of the cell
     * @return the cell value, or {@code null} for unknown columns
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expense expense = expenseList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return expense.getExpenseId();
            case 1:
                return String.format("%.2f EGP", expense.getAmount());
            case 2:
                return expense.getCategoryId();
            case 3:
                return dateFormat.format(expense.getTimestamp());
            case 4:
                return expense.getNotes();
            default:
                return null;
        }
    }
}