package Models;

import java.util.Date;

public class Expense {

    private int expenseId;
    private int cycleId;
    private double amount;
    private int categoryId;
    private String notes;
    private Date timestamp;
    // FIX: added userPin field — needed by TransactionDAO.saveExpense() because
    // the expenses table has a NOT NULL user_pin column
    private String userPin;

    public Expense(double amount, int categoryId, String notes) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.notes = notes;
        this.timestamp = new Date();
    }

    public boolean validateAmount() {
        if (this.amount <= 0) {
            System.out.println("Error: Amount must be greater than 0.");
            return false;
        }
        return true;
    }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getCycleId() { return cycleId; }
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getUserPin() { return userPin; }
    public void setUserPin(String userPin) { this.userPin = userPin; }

    public boolean editExpense(double amount, int categoryId, String notes) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.notes = notes;
        return true;
    }

    public boolean save() {
        return true;
    }
}
