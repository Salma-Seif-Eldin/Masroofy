package Models;

public class Category {
    private int categoryId;
    private String name;
    private String icon;

    // Constructor for creating a new category
    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    // Constructor for loading from database
    public Category(int categoryId, String name, String icon) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
    }

    // Standard Getters and Setters
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    /**
     * Used for populating JComboBoxes in ExpensesEntryActivity
     */
    @Override
    public String toString() {
        return name; 
    }
}