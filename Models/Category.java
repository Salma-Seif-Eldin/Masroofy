package Models;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private int categoryId;
    private String name;
    private String icon;

    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public Category(int categoryId, String name, String icon) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() { return name; }

    // ======================================================================
    // NEW: Single source of truth for category mapping
    // ======================================================================
    public static String getNameById(int id) {
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

    public static int getIdByName(String name) {
        return switch (name) {
            case "Food"          -> 1;
            case "Transport"     -> 2;
            case "Shopping"      -> 3;
            case "Health"        -> 4;
            case "Education"     -> 5;
            case "Entertainment" -> 6;
            default              -> 1;
        };
    }

    public static List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        names.add("Food");
        names.add("Transport");
        names.add("Shopping");
        names.add("Health");
        names.add("Education");
        names.add("Entertainment");
        return names;
    }
}