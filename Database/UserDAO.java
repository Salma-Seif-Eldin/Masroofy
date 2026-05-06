package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Data Access Object (DAO) for managing user PIN records in the database.
 * <p>
 * Provides methods to register new users, verify existing PINs,
 * and retrieve stored PIN values from the {@code users} table.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class UserDAO {

    /**
     * Constructs a new UserDAO for user PIN persistence operations.
     */
    public UserDAO() {
    }

    /**
     * Registers a new user by inserting their PIN into the database.
     *
     * @param pin the 4-digit PIN string to register
     * @return {@code true} if the PIN was successfully inserted; {@code false}
     *         otherwise
     */
    public boolean registerPin(String pin) {
        String sql = "INSERT INTO users (pin) VALUES (?)";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error registering PIN: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a given PIN already exists in the database.
     *
     * @param pin the PIN string to look up
     * @return {@code true} if the PIN exists; {@code false} otherwise
     */
    public boolean pinExists(String pin) {
        String sql = "SELECT pin FROM users WHERE pin = ?";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Error checking PIN: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the first PIN stored in the database.
     * <p>
     * Useful for single-user setups where only one PIN is expected.
     * </p>
     *
     * @return the first PIN string found, or {@code null} if no user exists
     */
    public String getFirstPin() {
        String sql = "SELECT pin FROM users LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getString("pin");
        } catch (Exception e) {
            System.out.println("Error fetching PIN: " + e.getMessage());
        }
        return null;
    }
}
