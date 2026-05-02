package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_NAME = "Masroofy.db";


    public static Connection connect() {
        // Returns a live connection to the SQLite database
        // Called by CycleDAO every time it runs a query
        try {
            Class.forName("org.sqlite.JDBC");
            // Loads the SQLite driver from sqlite-jdbc.jar

            return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return null;
        }
    }

  
    // CREATE TABLES

    public static void createTables() {
        // Called once on app startup from Main.java
        // Creates all tables if they don't already exist
        // Safe to call every launch — won't delete existing data

        String createCyclesTable =
            "CREATE TABLE IF NOT EXISTS budget_cycles (" +
            "cycle_id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "total_allowance REAL    NOT NULL,"                  +
            "start_date      TEXT    NOT NULL,"                  +
            "end_date        TEXT    NOT NULL"                   +
            ");";
        // cycle_id    — auto generated unique number
        // total_allowance — budget amount e.g. 5000.0
        // start_date  — stored as text "yyyy-MM-dd"
        // end_date    — stored as text "yyyy-MM-dd"

        String createExpensesTable =
            "CREATE TABLE IF NOT EXISTS expenses ("              +
            "expense_id  INTEGER PRIMARY KEY AUTOINCREMENT,"    +
            "cycle_id    INTEGER NOT NULL,"                      +
            "amount      REAL    NOT NULL,"                      +
            "category_id INTEGER NOT NULL,"                      +
            "notes       TEXT,"                                  +
            "date   TEXT    NOT NULL,"                      +
            "FOREIGN KEY (cycle_id) REFERENCES budget_cycles(cycle_id)" +
            ");";
        // expense_id  — auto generated unique number
        // cycle_id    — links expense to its budget cycle
        // amount      — how much was spent e.g. 150.0
        // category_id — links to categories table
        // notes       — optional description
        // timestamp   — when expense was recorded

        String createCategoriesTable =
            "CREATE TABLE IF NOT EXISTS categories ("            +
            "category_id INTEGER PRIMARY KEY AUTOINCREMENT,"    +
            "name        TEXT    NOT NULL,"                      +
            "icon        TEXT"                                   +
            ");";
        // Used by Member 2 for Category model

        String createAlertsTable =
            "CREATE TABLE IF NOT EXISTS alerts ("                +
            "id       INTEGER PRIMARY KEY AUTOINCREMENT,"        +
            "title    TEXT NOT NULL,"                            +
            "message  TEXT NOT NULL,"                            +
            "sent_at  TEXT NOT NULL"                             +
            ");";
        // Used by Member 3 for AlertManager

        // Run all CREATE TABLE statements
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createCyclesTable);
            System.out.println("✅ budget_cycles table ready.");

            stmt.execute(createExpensesTable);
            System.out.println("✅ expenses table ready.");

            stmt.execute(createCategoriesTable);
            System.out.println("✅ categories table ready.");

            stmt.execute(createAlertsTable);
            System.out.println("✅ alerts table ready.");

        } catch (Exception e) {
            System.out.println(" Error creating tables: " + e.getMessage());
        }
    }
}