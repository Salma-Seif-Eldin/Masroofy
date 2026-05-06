package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema initialization.
 * <p>
 * Provides a static factory method to open a connection to the local
 * {@code Masroofy.db} SQLite file, and a setup method to create all
 * required tables if they do not already exist.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class DatabaseManager {

    /**
     * Prevents instantiation of this utility class.
     */
    private DatabaseManager() {
    }

    private static final String DB_NAME = "Masroofy.db";

    /**
     * Opens and returns a new connection to the SQLite database.
     * Requires the SQLite JDBC driver to be present on the classpath.
     *
     * @return a {@link Connection} to the database, or {@code null} if the
     *         connection could not be established
     */
    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found! Add sqlite-jdbc JAR to Libraries.");
            return null;
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates all required application tables in the database if they do not
     * already exist.
     * Tables created:
     * <ul>
     * <li>{@code users} — stores user PINs</li>
     * <li>{@code budget_cycles} — stores budget cycle definitions per user</li>
     * <li>{@code expenses} — stores individual expense entries</li>
     * <li>{@code alerts} — stores sent alert records</li>
     * <li>{@code settings} — stores key-value application settings</li>
     * <li>{@code categories} — stores spending categories</li>
     * </ul>
     */
    public static void createTables() {
        Connection conn = connect();
        if (conn == null) {
            System.err.println("Cannot create tables: no database connection.");
            return;
        }

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "pin  TEXT PRIMARY KEY NOT NULL" +
                ");";

        String createCyclesTable = "CREATE TABLE IF NOT EXISTS budget_cycles (" +
                "cycle_id        INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_pin        TEXT    NOT NULL," +
                "total_allowance REAL    NOT NULL," +
                "start_date      TEXT    NOT NULL," +
                "end_date        TEXT    NOT NULL" +
                ");";

        String createExpensesTable = "CREATE TABLE IF NOT EXISTS expenses (" +
                "expense_id  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cycle_id    INTEGER NOT NULL," +
                "user_pin    TEXT    NOT NULL," +
                "amount      REAL    NOT NULL," +
                "category_id INTEGER NOT NULL," +
                "notes       TEXT," +
                "date        TEXT    NOT NULL," +
                "FOREIGN KEY (cycle_id) REFERENCES budget_cycles(cycle_id)" +
                ");";

        String createAlertsTable = "CREATE TABLE IF NOT EXISTS alerts (" +
                "id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_pin TEXT    NOT NULL," +
                "title    TEXT    NOT NULL," +
                "message  TEXT    NOT NULL," +
                "sent_at  TEXT    NOT NULL" +
                ");";

        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "key   TEXT PRIMARY KEY," +
                "value TEXT NOT NULL" +
                ");";

        String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories (" +
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
            System.out.println("All tables are ready.");
        } catch (Exception e) {
            System.err.println("Error creating tables: " + e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }
}