package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Customer;
import de.restaurant.model.Reservation;
import de.restaurant.service.CustomerService;
import de.restaurant.service.ReservationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * GUI-Panel für die Reservierungsverwaltung.
 * Ermöglicht das Anlegen, Anzeigen und Stornieren von Tischreservierungen.
 * Enthält jetzt einen interaktiven Saalplan zur visuellen Tischwahl.
 */
public class ReservationPanel extends JPanel implements Refreshable {

    // ---- Services ----
    private final ReservationService reservationService = new ReservationService();
    private final CustomerService    customerService    = new CustomerService();

    // ---- Saalplan & Tabelle ----
    private SaalplanPanel saalplanPanel;
    private JTable resTable;
    private DefaultTableModel tableModel;
    private JPanel centerCardPanel;
    private CardLayout cardLayout;

    // ---- Formular ----
    private JComboBox<Customer> cbCustomer;
    private JSpinner spTableNumber, spGuestCount;
    private JTextField tfDate, tfTime, tfNotes;
    private JButton btnAdd, btnCancel, btnClear, btnFilterToday, btnToggleView;
    private boolean showingSaalplan = true;

    private static final String[] COLUMNS = {
        "ID", "Datum", "Uhrzeit", "Tisch", "Kunde", "Personen", "Notizen"
    };

    /** Konstruktor */
    public ReservationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        // Zuerst das Formular initialisieren, da der Saalplan darauf verweist (spTableNumber)
        buildFormPanel();
        buildTable();
        loadData();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die Reservierungstabelle und den Saalplan in einem CardLayout */
    private void buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        resTable = new JTable(tableModel);
        resTable.setRowHeight(28);
        resTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resTable.setGridColor(new Color(220, 220, 235));

        resTable.getColumnModel().getColumn(0).setMaxWidth(50);
        resTable.getColumnModel().getColumn(3).setMaxWidth(60);
        resTable.getColumnModel().getColumn(5).setMaxWidth(70);

        JScrollPane sp = new JScrollPane(resTable);
        sp.setBorder(BorderFactory.createTitledBorder("Reservierungsliste"));

        // Interaktiven Saalplan erstellen
        saalplanPanel = new SaalplanPanel();
        saalplanPanel.setTableSelectionListener(tableNum -> {
            spTableNumber.setValue(tableNum);
        });

        // CardLayout für das Umschalten
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBackground(new Color(248, 249, 255));
        centerCardPanel.add(saalplanPanel, "SAALPLAN");
        centerCardPanel.add(sp, "TABLE");

        add(centerCardPanel, BorderLayout.CENTER);
    }

    /** Erstellt das Formular zum Anlegen neuer Reservierungen */
    private void buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Neue Reservierung"));
        formPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Kunde
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Kunde:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cbCustomer = new JComboBox<>();
        formPanel.add(cbCustomer, gbc);

        // Tisch
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Tisch-Nr. *:"), gbc);
        gbc.gridx = 1;
        spTableNumber = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        formPanel.add(spTableNumber, gbc);

        // Datum
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Datum * (JJJJ-MM-TT):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfDate = new JTextField(LocalDate.now().toString());
        formPanel.add(tfDate, gbc);

        // Uhrzeit
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Uhrzeit * (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfTime = new JTextField("18:00");
        formPanel.add(tfTime, gbc);

        // Personen
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Personen *:"), gbc);
        gbc.gridx = 1;
        spGuestCount = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        formPanel.add(spGuestCount, gbc);

        // Notizen
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Notizen:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfNotes = new JTextField();
        formPanel.add(tfNotes, gbc);

        // Trennlinie
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Buttons
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        btnPanel.setBackground(Color.WHITE);

        btnAdd         = createButton("📅 Reservierung anlegen", new Color(46, 139, 87));
        btnCancel      = createButton("❌ Stornieren",            new Color(178, 34, 34));
        btnClear       = createButton("✖ Leeren",                 new Color(120, 120, 120));

        btnPanel.add(btnAdd);
        btnPanel.add(btnCancel);
        btnPanel.add(btnClear);
        formPanel.add(btnPanel, gbc);

        // Filter- und Switch-Buttons oben links
        JPanel topBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBtn.setBackground(new Color(248, 249, 255));
        
        btnFilterToday = createButton("Heute anzeigen", new Color(70, 100, 160));
        JButton btnAll = createButton("Alle anzeigen", new Color(100, 100, 120));
        btnToggleView  = createButton("📋 Listenansicht anzeigen", new Color(110, 110, 130));
        
        topBtn.add(btnFilterToday);
        topBtn.add(btnAll);
        topBtn.add(btnToggleView);
        add(topBtn, BorderLayout.NORTH);

        add(formPanel, BorderLayout.EAST);

        // Event-Handler
        btnAdd.addActionListener(e         -> addReservation());
        btnCancel.addActionListener(e      -> cancelReservation());
        btnClear.addActionListener(e       -> clearForm());
        btnFilterToday.addActionListener(e -> filterToday());
        btnAll.addActionListener(e         -> loadData());
        btnToggleView.addActionListener(e  -> toggleView());

        // Hinzufügen von Fokus- und Action-Listenern, damit der Saalplan bei Datumsänderung aktualisiert wird
        tfDate.addActionListener(e -> updateFloorPlan());
        tfDate.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateFloorPlan();
            }
        });

        loadCustomers();
    }

    /** Umschalten zwischen Saalplan und Tabelle */
    private void toggleView() {
        showingSaalplan = !showingSaalplan;
        if (showingSaalplan) {
            cardLayout.show(centerCardPanel, "SAALPLAN");
            btnToggleView.setText("📋 Listenansicht anzeigen");
        } else {
            cardLayout.show(centerCardPanel, "TABLE");
            btnToggleView.setText("🗺 Interaktiver Saalplan");
        }
    }

    /** Aktualisiert die Farbbelegung des Saalplans basierend auf dem eingetragenen Datum */
    private void updateFloorPlan() {
        if (saalplanPanel == null) return;
        try {
            LocalDate date = LocalDate.parse(tfDate.getText().trim());
            List<Reservation> dayReservations = reservationService.getReservationsByDate(date);
            saalplanPanel.setReservations(dayReservations, date);
        } catch (Exception e) {
            // Fallback auf heute
            List<Reservation> dayReservations = reservationService.getReservationsByDate(LocalDate.now());
            saalplanPanel.setReservations(dayReservations, LocalDate.now());
        }
    }

    // ---- Aktionen ----

    /** Lädt alle Kunden in die ComboBox */
    private void loadCustomers() {
        cbCustomer.removeAllItems();
        cbCustomer.addItem(null); // Laufkundschaft
        customerService.getAllCustomers().forEach(cbCustomer::addItem);
    }

    /** Lädt alle Reservierungen in die Tabelle und aktualisiert den Saalplan */
    private void loadData() {
        tableModel.setRowCount(0);
        List<Reservation> list = reservationService.getAllReservations();
        fillTable(list);
        updateFloorPlan();
    }

    /** Zeigt nur die heutigen Reservierungen */
    private void filterToday() {
        tableModel.setRowCount(0);
        tfDate.setText(LocalDate.now().toString());
        List<Reservation> list = reservationService.getReservationsByDate(LocalDate.now());
        fillTable(list);
        updateFloorPlan();
    }

    /** Füllt die Tabelle mit einer Reservierungsliste */
    private void fillTable(List<Reservation> list) {
        for (Reservation r : list) {
            String customerName = (r.getCustomer() != null) ? r.getCustomer().getName() : "Laufkundschaft";
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getReservationDate().toString(),
                    r.getReservationTime().toString(),
                    r.getTableNumber(),
                    customerName,
                    r.getGuestCount(),
                    r.getNotes()
            });
        }
    }

    /** Legt eine neue Reservierung an */
    private void addReservation() {
        try {
            Customer customer   = (Customer) cbCustomer.getSelectedItem();
            int tableNumber     = (int) spTableNumber.getValue();
            LocalDate date      = LocalDate.parse(tfDate.getText().trim());
            LocalTime time      = LocalTime.parse(tfTime.getText().trim());
            int guestCount      = (int) spGuestCount.getValue();
            String notes        = tfNotes.getText();

            // Prüfen, ob Tisch an diesem Datum bereits belegt ist
            List<Reservation> dayReservations = reservationService.getReservationsByDate(date);
            for (Reservation res : dayReservations) {
                if (res.getTableNumber() == tableNumber) {
                    throw new ValidationException("Tisch #" + tableNumber + " ist am " + date + " bereits reserviert!");
                }
            }

            Reservation r = reservationService.createReservation(customer, tableNumber, date, time, guestCount, notes);
            JOptionPane.showMessageDialog(this,
                    "Reservierung #" + r.getId() + " erfolgreich angelegt!",
                    "Erfolg", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            clearForm();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Ungültiges Datums- oder Zeitformat. Bitte JJJJ-MM-TT bzw. HH:MM verwenden.");
        }
    }

    /** Storniert die ausgewählte Reservierung */
    private void cancelReservation() {
        int id = -1;
        
        if (showingSaalplan) {
            // Aus dem Saalplan stornieren
            int tableNum = saalplanPanel.getSelectedTableNumber();
            if (tableNum < 1) {
                showError("Bitte wählen Sie zuerst einen Tisch auf dem Saalplan aus.");
                return;
            }
            
            try {
                LocalDate date = LocalDate.parse(tfDate.getText().trim());
                List<Reservation> dayReservations = reservationService.getReservationsByDate(date);
                for (Reservation res : dayReservations) {
                    if (res.getTableNumber() == tableNum) {
                        id = res.getId();
                        break;
                    }
                }
            } catch (Exception e) {
                // date parse error
            }
            
            if (id == -1) {
                showError("Dieser Tisch ist am ausgewählten Datum nicht reserviert.");
                return;
            }
        } else {
            // Aus der Tabelle stornieren
            int row = resTable.getSelectedRow();
            if (row < 0) { showError("Bitte eine Reservierung in der Liste auswählen."); return; }
            id = (int) tableModel.getValueAt(row, 0);
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reservierung #" + id + " wirklich stornieren?",
                "Stornieren bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            reservationService.cancelReservation(id);
            loadData();
            clearForm();
        }
    }

    /** Leert das Eingabeformular */
    private void clearForm() {
        cbCustomer.setSelectedIndex(0);
        spTableNumber.setValue(1);
        tfDate.setText(LocalDate.now().toString());
        tfTime.setText("18:00");
        spGuestCount.setValue(2);
        tfNotes.setText("");
        resTable.clearSelection();
        if (saalplanPanel != null) {
            saalplanPanel.setSelectedTableNumber(-1);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 35));
        return btn;
    }

    @Override
    public void refresh() {
        loadData();
        loadCustomers();
    }
}
