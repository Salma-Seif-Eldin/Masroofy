package Models;

import java.util.Date;

public class Expense {

    private int expenseId;      
    private int cycleId;         
    private double amount;
    private int categoryId;      
    private Date timestamp;
    private String notes;

   

      // FULL constructor — used when loading from database
     // All fields provided because they were saved previously
    public Expense(int expenseId, int cycleId, double amount, int categoryId, String notes, Date timestamp) 
    {
        this.expenseId  = expenseId;
        this.cycleId    = cycleId;
        this.amount     = amount;
        this.categoryId = categoryId;
        this.notes      = notes;
        this.timestamp  = timestamp;
    }

    // SHORT constructor — used when user creates new expense
    // No ID yet (database hasn't assigned one)
    // No cycleId yet (BudgetManager sets it after)
    public Expense(double amount, int categoryId, String notes) {
        this.amount     = amount;
        this.categoryId = categoryId;
        this.notes      = notes;
        this.timestamp  = new Date(); //records current date and time
    }
//getters and setters

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getCycleId() { return cycleId; }
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }


    
    public boolean validateAmount(double amount) {
        return amount > 0;
    }

   
    public boolean validateDate(Date date) {
        if (date == null) return false;
        return !date.after(new Date()); // date must be today or in the past
    }

    // formats amount as currency string 
    public String formatCurrency() {
        return String.format("EGP %.2f", this.amount);
    }

    //checks amount
    public boolean editExpense(double newAmount, int newCategoryId, String newNotes) {
        if (!validateAmount(newAmount)) {
            System.out.println("Invalid amount: must be greater than 0");
            return false;
        }
        this.amount     = newAmount;
        this.categoryId = newCategoryId;
        this.notes      = newNotes;
        return true;
    }

    
    public boolean delete() {// Can't delete something that was never saved to DB
        if (this.expenseId <= 0) {
            System.out.println("Cannot delete: expense has no valid ID");
            return false;
        }
        // CycleDAO.deleteExpense(this.expenseId) is called from BudgetManager
        System.out.println("Expense marked for deletion: " + this.expenseId);
        return true;
    }

    // Validates before saving — actual DB save handled by CycleDAO
    public boolean save() {
        if (!validateAmount(this.amount)) {
            System.out.println("Cannot save: invalid amount");
            return false;
        }
        if (!validateDate(this.timestamp)) {
            System.out.println("Cannot save: invalid date");
            return false;
        }
        // CycleDAO.insertExpense(this) is called from BudgetManager
        System.out.println("Expense ready to save: " + formatCurrency());
        return true;
    }

    // ─────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────

    // ✅ Useful for debugging and logging
    @Override
    public String toString() {
        return "Expense{" +
                "id=" + expenseId +
                ", cycleId=" + cycleId +
                ", amount=" + formatCurrency() +
                ", categoryId=" + categoryId +
                ", notes='" + notes + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}