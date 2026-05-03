package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_NAME = "Masroofy.db";

    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC driver not found! Add sqlite-jdbc JAR to Libraries.");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            return null;
        }
    }

    public static void createTables() {
        Connection conn = connect();
        if (conn == null) {
            System.err.println("❌ Cannot create tables: no database connection.");
            return;
        }

        // ── NEW: one row per registered user PIN ──────────────────────────────
        String createUsersTable =
            "CREATE TABLE IF NOT EXISTS users (" +
            "pin  TEXT PRIMARY KEY NOT NULL" +
            ");";

        String createCyclesTable =
            "CREATE TABLE IF NOT EXISTS budget_cycles (" +
            "cycle_id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_pin        TEXT    NOT NULL," +
            "total_allowance REAL    NOT NULL," +
            "start_date      TEXT    NOT NULL," +
            "end_date        TEXT    NOT NULL" +
            ");";

        String createExpensesTable =
            "CREATE TABLE IF NOT EXISTS expenses (" +
            "expense_id  INTEGER PRIMARY KEY AUTOINCREMENT," +
            "cycle_id    INTEGER NOT NULL," +
            "user_pin    TEXT    NOT NULL," +
            "amount      REAL    NOT NULL," +
            "category_id INTEGER NOT NULL," +
            "notes       TEXT," +
            "date        TEXT    NOT NULL," +
            "FOREIGN KEY (cycle_id) REFERENCES budget_cycles(cycle_id)" +
            ");";

        String createAlertsTable =
            "CREATE TABLE IF NOT EXISTS alerts (" +
            "id       INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_pin TEXT    NOT NULL," +
            "title    TEXT    NOT NULL," +
            "message  TEXT    NOT NULL," +
            "sent_at  TEXT    NOT NULL" +
            ");";

        // settings kept for backward-compat but no longer used for PIN storage
        String createSettingsTable =
            "CREATE TABLE IF NOT EXISTS settings (" +
            "key   TEXT PRIMARY KEY," +
            "value TEXT NOT NULL" +
            ");";

        String createCategoriesTable =
            "CREATE TABLE IF NOT EXISTS categories (" +
            "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name        TEXT    NOT NULL," +
            "icon        TEXT" +
            ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCyclesTable);
            stmt.execute(createExpensesTable);
            stmt.execute(createAlertsTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createCategoriesTable);
            System.out.println("✅ All tables are ready.");
        } catch (Exception e) {
            System.err.println("❌ Error creating tables: " + e.getMessage());
        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }
}
