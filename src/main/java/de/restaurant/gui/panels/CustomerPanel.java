package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Customer;
import de.restaurant.service.CustomerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI-Panel für die Kundenverwaltung.
 * Ermöglicht das Anlegen, Bearbeiten, Suchen und Löschen von Kunden.
 */
public class CustomerPanel extends JPanel implements Refreshable {

    // ---- Service ----
    private final CustomerService customerService = new CustomerService();

    // ---- Tabelle ----
    private JTable customerTable;
    private DefaultTableModel tableModel;

    // ---- Formular ----
    private JTextField tfName, tfPhone, tfEmail, tfSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;

    private static final String[] COLUMNS = {"ID", "Name", "Telefon", "E-Mail"};

    /** Konstruktor */
    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        buildSearchBar();
        buildTable();
        buildFormAndButtons();
        loadData();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die Suchleiste */
    private void buildSearchBar() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(248, 249, 255));

        tfSearch = new JTextField(25);
        tfSearch.setToolTipText("Suche nach Name, Telefon oder E-Mail");
        btnSearch = createButton("🔍 Suchen", new Color(70, 130, 180));

        searchPanel.add(new JLabel("Suche:"));
        searchPanel.add(tfSearch);
        searchPanel.add(btnSearch);

        JButton btnShowAll = createButton("Alle anzeigen", new Color(100, 100, 120));
        searchPanel.add(btnShowAll);

        add(searchPanel, BorderLayout.NORTH);

        btnSearch.addActionListener(e  -> searchCustomers());
        btnShowAll.addActionListener(e -> loadData());
        // Suche auch bei Enter auslösen
        tfSearch.addActionListener(e  -> searchCustomers());
    }

    /** Erstellt die Kundentabelle */
    private void buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(28);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setGridColor(new Color(220, 220, 235));

        customerTable.getColumnModel().getColumn(0).setMaxWidth(50);

        // Auswahl → Formular befüllen
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        JScrollPane sp = new JScrollPane(customerTable);
        sp.setBorder(BorderFactory.createTitledBorder("Kundenliste"));
        add(sp, BorderLayout.CENTER);
    }

    /** Erstellt Formular und Buttons */
    private void buildFormAndButtons() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(248, 249, 255));

        // Formular
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Kundendaten"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        tfName = new JTextField(20);
        formPanel.add(tfName, gbc);

        // Telefon
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfPhone = new JTextField(20);
        formPanel.add(tfPhone, gbc);

        // E-Mail
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("E-Mail:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfEmail = new JTextField(20);
        formPanel.add(tfEmail, gbc);

        rightPanel.add(formPanel, BorderLayout.NORTH);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 0, 6));
        btnPanel.setBackground(new Color(248, 249, 255));
        btnPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        btnAdd    = createButton("➕ Hinzufügen",    new Color(46, 139, 87));
        btnUpdate = createButton("✏️ Aktualisieren",  new Color(70, 130, 180));
        btnDelete = createButton("🗑 Löschen",        new Color(178, 34, 34));
        btnClear  = createButton("✖ Formular leeren", new Color(120, 120, 120));

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        rightPanel.add(btnPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Event-Handler
        btnAdd.addActionListener(e    -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e  -> clearForm());
    }

    // ---- Aktionen ----

    /** Lädt alle Kunden in die Tabelle */
    private void loadData() {
        tableModel.setRowCount(0);
        List<Customer> customers = customerService.getAllCustomers();

        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getName(), c.getPhone(), c.getEmail()
            });
        }
    }

    /** Sucht Kunden nach dem eingegebenen Begriff */
    private void searchCustomers() {
        tableModel.setRowCount(0);
        List<Customer> results = customerService.searchCustomers(tfSearch.getText());

        for (Customer c : results) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getName(), c.getPhone(), c.getEmail()
            });
        }
    }

    /** Legt einen neuen Kunden an */
    private void addCustomer() {
        try {
            customerService.createCustomer(tfName.getText(), tfPhone.getText(), tfEmail.getText());
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Kunde erfolgreich angelegt.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /** Aktualisiert den ausgewählten Kunden */
    private void updateCustomer() {
        int row = customerTable.getSelectedRow();
        if (row < 0) { showError("Bitte einen Kunden auswählen."); return; }

        try {
            int id = (int) tableModel.getValueAt(row, 0);
            Customer c = new Customer(id, tfName.getText(), tfPhone.getText(), tfEmail.getText());
            customerService.updateCustomer(c);
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Kunde erfolgreich aktualisiert.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /** Löscht den ausgewählten Kunden */
    private void deleteCustomer() {
        int row = customerTable.getSelectedRow();
        if (row < 0) { showError("Bitte einen Kunden auswählen."); return; }

        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Kunden \"" + name + "\" wirklich löschen?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            customerService.deleteCustomer(id);
            loadData();
            clearForm();
        }
    }

    /** Füllt das Formular aus der ausgewählten Zeile */
    private void fillFormFromTable() {
        int row = customerTable.getSelectedRow();
        if (row < 0) return;
        tfName.setText((String) tableModel.getValueAt(row, 1));
        tfPhone.setText((String) tableModel.getValueAt(row, 2));
        tfEmail.setText((String) tableModel.getValueAt(row, 3));
    }

    /** Leert das Eingabeformular */
    private void clearForm() {
        tfName.setText("");
        tfPhone.setText("");
        tfEmail.setText("");
        customerTable.clearSelection();
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
        btn.setPreferredSize(new Dimension(180, 35));
        return btn;
    }

    @Override
    public void refresh() {
        loadData();
    }
}
