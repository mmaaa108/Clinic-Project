// models/Appointment.java
package models;

public class Appointment {

    private String id;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;

    private String date; // yyyy-MM-dd
    private String time; // HH:mm

    public Appointment(String id, String patientName, String doctorName, String date, String time) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    // لتوافق TableColumn bindings الحالية
    public String getDateString() { return date; }
    public String getTimeString() { return time; }
}
