package dao;

import models.Doctor;
import utils.DB;

import java.sql.*;
import java.util.*;

public class DoctorDAO {

    public List<Doctor> findAll() {
        String sql = "SELECT id, name, specialty FROM Doctors ORDER BY id";
        List<Doctor> list = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Doctor(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name"),
                        rs.getString("specialty")
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public int insert(Doctor d) {
        String sql = "INSERT INTO Doctors(name, specialty) VALUES (?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialty());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    public void update(Doctor d) {
        String sql = "UPDATE Doctors SET name=?, specialty=? WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialty());
            ps.setInt(3, Integer.parseInt(d.getId()));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Doctors WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM Doctors";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Map<String,Integer> nameToIdMap() {
        String sql = "SELECT id, name FROM Doctors";
        Map<String,Integer> map = new HashMap<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("id"));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return map;
    }
}
