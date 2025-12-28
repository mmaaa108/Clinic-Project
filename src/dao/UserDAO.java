package dao;

import utils.DB;
import java.sql.*;

public class UserDAO {

    public String findFirstNameByEmailAndHash(String email, String md5Hash) {
        String sql = "SELECT first_name FROM Users WHERE email=? AND password_hash=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, md5Hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM Users WHERE email=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void insert(String firstName, String lastName, String email, String md5Hash) {
        String sql = "INSERT INTO Users(first_name,last_name,email,password_hash) VALUES (?,?,?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, md5Hash);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
