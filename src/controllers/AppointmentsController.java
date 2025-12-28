package controllers;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Appointment;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppointmentsController {

    // UI
    @FXML private ComboBox<String> patientComboBox;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> idColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;

    // Phase 2 أدوات البحث/الفرز
    @FXML private TextField  searchField;        // اسم مريض/طبيب
    @FXML private DatePicker searchDatePicker;   // فلترة بالتاريخ
    @FXML private ComboBox<String> sortComboBox; // "التاريخ" أو "الوقت"

    // Data
    private final ObservableList<Appointment> appointmentListView = FXCollections.observableArrayList();
    private final List<Appointment> appointmentsAll = new ArrayList<>();

    // Maps (اسم → ID) من DB
    private Map<String,Integer> patientNameToId = new HashMap<>();
    private Map<String,Integer> doctorNameToId  = new HashMap<>();

    // DAO
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
//
    @FXML
private void handleSearchAppointments(javafx.event.ActionEvent e) {
    applyAppointmentFilters();
}

@FXML
private void handleSortAppointments(javafx.event.ActionEvent e) {
    applyAppointmentFilters();
}

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        // تعبئة الكومبو من DB
        try {
            patientNameToId = patientDAO.nameToIdMap();
            doctorNameToId  = doctorDAO.nameToIdMap();
            patientComboBox.getItems().setAll(new TreeSet<>(patientNameToId.keySet()));
            doctorComboBox.getItems().setAll(new TreeSet<>(doctorNameToId.keySet()));
        } catch (RuntimeException e) {
            error("DB error loading names:\n" + e.getMessage());
        }

        // تحميل المواعيد من DB
        loadAppointments();

        // Phase 2: تهيئة البحث/الفرز
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList("التاريخ", "الوقت"));
            sortComboBox.setOnAction(e -> applyAppointmentFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyAppointmentFilters());
        }
        if (searchDatePicker != null) {
            searchDatePicker.valueProperty().addListener((obs, o, n) -> applyAppointmentFilters());
        }
    }

    private void loadAppointments() {
        try {
            appointmentsAll.clear();
            appointmentsAll.addAll(appointmentDAO.findAllWithNames());
            appointmentListView.setAll(appointmentsAll);
            appointmentsTable.setItems(appointmentListView);
        } catch (RuntimeException e) {
            error("Database error while loading appointments:\n" + e.getMessage());
        }
    }

    private boolean isValidHHmm(String s) {
        return s != null && Pattern.matches("([01]\\d|2[0-3]):[0-5]\\d", s);
    }

    @FXML
    private void handleSchedule() {
        String patientName = patientComboBox.getValue();
        String doctorName  = doctorComboBox.getValue();
        LocalDate date     = datePicker.getValue();
        String time        = (timeField.getText() == null) ? "" : timeField.getText().trim();

        if (patientName == null || doctorName == null || date == null || time.isEmpty()) {
            error("كل الحقول مطلوبة."); return;
        }
        if (!isValidHHmm(time)) { error("صيغة الوقت غير صحيحة. HH:MM"); return; }
        if (date.isBefore(LocalDate.now())) { error("التاريخ يجب أن يكون مستقبلي."); return; }

        try {
            Integer pid = patientNameToId.get(patientName);
            Integer did = doctorNameToId.get(doctorName);
            if (pid == null || did == null) { error("تعذّر إيجاد المريض/الطبيب المختار."); return; }

            int newId = appointmentDAO.insert(pid, did, date, time);
            appointmentsAll.add(new Appointment(String.valueOf(newId), patientName, doctorName, date.toString(), time));
            applyAppointmentFilters();
            info("The appointment has been booked.");
            clearFields();
        } catch (RuntimeException e) {
            error("DB insert failed:\n" + e.getMessage());
        }
    }

    // فلترة + فرز (موحّد) + Live Search
    private void applyAppointmentFilters() {
        String kw = (searchField == null ? "" : searchField.getText()).trim().toLowerCase();
        LocalDate dt = (searchDatePicker == null) ? null : searchDatePicker.getValue();

        List<Appointment> filtered = appointmentsAll.stream()
                .filter(a -> kw.isEmpty()
                        || (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(kw))
                        || (a.getDoctorName()  != null && a.getDoctorName().toLowerCase().contains(kw)))
                .filter(a -> dt == null || dt.toString().equals(a.getDate()))
                .collect(Collectors.toList());

        String sort = (sortComboBox == null) ? null : sortComboBox.getValue();
        if ("التاريخ".equals(sort)) {
            filtered = filtered.stream()
                    .sorted((x, y) -> {
                        try { return LocalDate.parse(x.getDate()).compareTo(LocalDate.parse(y.getDate())); }
                        catch (Exception e) { return x.getDate().compareToIgnoreCase(y.getDate()); }
                    })
                    .collect(Collectors.toList());
        } else if ("الوقت".equals(sort)) {
            filtered = filtered.stream()
                    .sorted((x, y) -> {
                        try { return LocalTime.parse(x.getTime()).compareTo(LocalTime.parse(y.getTime())); }
                        catch (Exception e) { return x.getTime().compareToIgnoreCase(y.getTime()); }
                    })
                    .collect(Collectors.toList());
        }

        appointmentListView.setAll(filtered);
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    private void clearFields() {
        patientComboBox.setValue(null);
        doctorComboBox.setValue(null);
        datePicker.setValue(null);
        timeField.clear();

        if (searchField != null) searchField.clear();
        if (searchDatePicker != null) searchDatePicker.setValue(null);
        if (sortComboBox != null) sortComboBox.setValue(null);

        appointmentListView.setAll(appointmentsAll);
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
