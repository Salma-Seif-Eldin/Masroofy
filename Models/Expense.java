package Models;

import java.util.Date;

/**
 * Represents a single expense entry recorded by the user.
 * <p>
 * Each expense belongs to a budget cycle and is classified under a category.
 * It stores the amount, optional notes, a timestamp, and the PIN of the user
 * who created it.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class Expense {

    private int expenseId;
    private int cycleId;
    private double amount;
    private int categoryId;
    private String notes;
    private Date timestamp;
    private String userPin;

    /**
     * Constructs a new Expense with the given amount, category, and notes.
     * The timestamp is automatically set to the current date and time.
     *
     * @param amount     the expense amount (must be greater than 0)
     * @param categoryId the ID of the category this expense belongs to
     * @param notes      optional notes describing the expense
     */
    public Expense(double amount, int categoryId, String notes) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.notes = notes;
        this.timestamp = new Date();
    }

    /**
     * Validates that the expense amount is greater than zero.
     *
     * @return {@code true} if the amount is valid; {@code false} otherwise
     */
    public boolean validateAmount() {
        if (this.amount <= 0) {
            System.out.println("Error: Amount must be greater than 0.");
            return false;
        }
        return true;
    }

    /**
     * Returns the unique identifier of this expense.
     *
     * @return the expense ID
     */
    public int getExpenseId() { return expenseId; }

    /**
     * Sets the unique identifier of this expense.
     *
     * @param expenseId the expense ID to set
     */
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    /**
     * Returns the ID of the budget cycle this expense belongs to.
     *
     * @return the cycle ID
     */
    public int getCycleId() { return cycleId; }

    /**
     * Sets the ID of the budget cycle this expense belongs to.
     *
     * @param cycleId the cycle ID to set
     */
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    /**
     * Returns the monetary amount of this expense.
     *
     * @return the expense amount
     */
    public double getAmount() { return amount; }

    /**
     * Sets the monetary amount of this expense.
     *
     * @param amount the amount to set
     */
    public void setAmount(double amount) { this.amount = amount; }

    /**
     * Returns the category ID associated with this expense.
     *
     * @return the category ID
     */
    public int getCategoryId() { return categoryId; }

    /**
     * Sets the category ID for this expense.
     *
     * @param categoryId the category ID to set
     */
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    /**
     * Returns the optional notes attached to this expense.
     *
     * @return the notes string, or {@code null} if none
     */
    public String getNotes() { return notes; }

    /**
     * Sets the optional notes for this expense.
     *
     * @param notes the notes to set
     */
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Returns the timestamp when this expense was created.
     *
     * @return the expense creation date and time
     */
    public Date getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp for this expense.
     *
     * @param timestamp the date and time to set
     */
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    /**
     * Returns the PIN of the user who recorded this expense.
     *
     * @return the user's PIN string
     */
    public String getUserPin() { return userPin; }

    /**
     * Sets the PIN of the user who recorded this expense.
     * Required by {@code TransactionDAO.saveExpense()} due to the NOT NULL constraint
     * on the {@code user_pin} column in the expenses table.
     *
     * @param userPin the user PIN to associate with this expense
     */
    public void setUserPin(String userPin) { this.userPin = userPin; }

    /**
     * Updates this expense with new values for amount, category, and notes.
     *
     * @param amount     the new expense amount
     * @param categoryId the new category ID
     * @param notes      the new notes
     * @return {@code true} always, indicating the edit was applied
     */
    public boolean editExpense(double amount, int categoryId, String notes) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.notes = notes;
        return true;
    }

    /**
     * Placeholder method for persisting the expense.
     *
     * @return {@code true} always
     */
    public boolean save() {
        return true;
    }
}