package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a spending category used to classify expenses.
 * <p>
 * Each category has a unique ID, a display name, and an icon string.
 * This class also provides static utility methods for mapping between
 * category IDs and names, serving as the single source of truth for
 * category data throughout the application.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class Category {
    private int categoryId;
    private String name;
    private String icon;

    /**
     * Constructs a Category without an ID (used before saving to the database).
     *
     * @param name the display name of the category
     * @param icon the icon string representing the category
     */
    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    /**
     * Constructs a Category with a known database ID.
     *
     * @param categoryId the unique identifier for the category
     * @param name       the display name of the category
     * @param icon       the icon string representing the category
     */
    public Category(int categoryId, String name, String icon) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
    }

    /**
     * Returns the unique identifier of this category.
     *
     * @return the category ID
     */
    public int getCategoryId() { return categoryId; }

    /**
     * Sets the unique identifier of this category.
     *
     * @param categoryId the category ID to set
     */
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    /**
     * Returns the display name of this category.
     *
     * @return the category name
     */
    public String getName() { return name; }

    /**
     * Sets the display name of this category.
     *
     * @param name the category name to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the icon string for this category.
     *
     * @return the icon representation
     */
    public String getIcon() { return icon; }

    /**
     * Sets the icon string for this category.
     *
     * @param icon the icon to set
     */
    public void setIcon(String icon) { this.icon = icon; }

    /**
     * Returns the name of this category as its string representation.
     *
     * @return the category name
     */
    @Override
    public String toString() { return name; }

    /**
     * Returns the category name corresponding to the given ID.
     *
     * @param id the category ID (1–6)
     * @return the category name, or {@code "Other"} if the ID is not recognized
     */
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

    /**
     * Returns the category ID corresponding to the given name.
     *
     * @param name the category name (e.g., "Food", "Transport")
     * @return the corresponding category ID, or {@code 1} (Food) if the name is not recognized
     */
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

    /**
     * Returns a list of all predefined category names.
     *
     * @return a {@link List} containing all category name strings
     */
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