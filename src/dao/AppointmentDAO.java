package dao;

import models.Appointment;
import utils.DB;

import java.sql.*;
import java.util.*;

public class AppointmentDAO {

    public List<Appointment> findAllWithNames() {
        String sql = """
            SELECT a.id, p.name AS patientName, d.name AS doctorName, a.date, a.time
            FROM Appointments a
            JOIN Patients p ON p.id = a.patient_id
            JOIN Doctors  d ON d.id = a.doctor_id
            ORDER BY a.id
        """;
        List<Appointment> list = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Appointment(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("patientName"),
                        rs.getString("doctorName"),
                        rs.getString("date"),
                        rs.getString("time")
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public int insert(int patientId, int doctorId, java.time.LocalDate date, String time) {
        String sql = "INSERT INTO Appointments(patient_id, doctor_id, date, time) VALUES (?,?,?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setString(4, time);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    public void update(int id, int patientId, int doctorId, java.time.LocalDate date, String time) {
        String sql = "UPDATE Appointments SET patient_id=?, doctor_id=?, date=?, time=? WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setString(4, time);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Appointments WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM Appointments";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
