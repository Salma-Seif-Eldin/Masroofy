package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

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

    public String getFirstPin() {
        String sql = "SELECT pin FROM users LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("pin");
        } catch (Exception e) {
            System.out.println("Error fetching PIN: " + e.getMessage());
        }
        return null;
    }
}