package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.MenuItem;
import de.restaurant.service.MenuService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI-Panel für die Speisekartenverwaltung.
 * Ermöglicht das Anzeigen, Hinzufügen, Bearbeiten und Löschen von Gerichten.
 */
public class MenuPanel extends JPanel implements Refreshable {

    // ---- Service ----
    private final MenuService menuService = new MenuService();

    // ---- GUI-Komponenten ----
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfPrice, tfDescription;
    private JComboBox<String> cbCategory;
    private JCheckBox chkAvailable;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // ---- Tabellenspalten ----
    private static final String[] COLUMNS = {"ID", "Name", "Kategorie", "Preis (€)", "Beschreibung", "Verfügbar"};

    /** Konstruktor: Erstellt und initialisiert das Panel */
    public MenuPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        buildTable();
        buildForm();
        buildButtons();
        loadData();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die Tabelle mit Scrollbereich */
    private void buildTable() {
        // Tabellenmodell – ID-Spalte nicht editierbar
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        menuTable = new JTable(tableModel);
        menuTable.setRowHeight(28);
        menuTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.setGridColor(new Color(220, 220, 235));
        menuTable.setShowGrid(true);

        // Spaltenbreiten
        menuTable.getColumnModel().getColumn(0).setMaxWidth(50);   // ID
        menuTable.getColumnModel().getColumn(3).setMaxWidth(100);  // Preis
        menuTable.getColumnModel().getColumn(5).setMaxWidth(90);   // Verfügbar

        // Zeile auswählen → Formular befüllen
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Aktuelle Speisekarte"));
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Erstellt das Eingabeformular für neue/bearbeitete Gerichte */
    private void buildForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));
        formPanel.setBorder(BorderFactory.createTitledBorder("Gericht bearbeiten / hinzufügen"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tfName = new JTextField(20);
        formPanel.add(tfName, gbc);

        // Kategorie
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Kategorie *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cbCategory = new JComboBox<>(MenuService.CATEGORIES);
        formPanel.add(cbCategory, gbc);

        // Preis
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Preis (€) *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfPrice = new JTextField(10);
        formPanel.add(tfPrice, gbc);

        // Beschreibung
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Beschreibung:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        tfDescription = new JTextField(30);
        formPanel.add(tfDescription, gbc);

        // Verfügbar
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Verfügbar:"), gbc);
        gbc.gridx = 1;
        chkAvailable = new JCheckBox();
        chkAvailable.setSelected(true);
        chkAvailable.setBackground(Color.WHITE);
        formPanel.add(chkAvailable, gbc);

        add(formPanel, BorderLayout.EAST);
    }

    /** Erstellt die Aktions-Buttons */
    private void buildButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(new Color(248, 249, 255));

        btnAdd    = createButton("➕ Hinzufügen",  new Color(46, 139, 87));
        btnUpdate = createButton("✏️ Aktualisieren", new Color(70, 130, 180));
        btnDelete = createButton("🗑 Löschen",      new Color(178, 34, 34));
        btnClear  = createButton("✖ Leeren",        new Color(120, 120, 120));

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        add(btnPanel, BorderLayout.SOUTH);

        // Event-Handler
        btnAdd.addActionListener(e    -> addMenuItem());
        btnUpdate.addActionListener(e -> updateMenuItem());
        btnDelete.addActionListener(e -> deleteMenuItem());
        btnClear.addActionListener(e  -> clearForm());
    }

    // ---- Aktionen ----

    /** Lädt alle Gerichte aus der Datenbank und befüllt die Tabelle */
    private void loadData() {
        tableModel.setRowCount(0);
        List<MenuItem> items = menuService.getAllMenuItems();

        for (MenuItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("%.2f", item.getPrice()),
                    item.getDescription(),
                    item.isAvailable() ? "Ja" : "Nein"
            });
        }
    }

    /** Fügt ein neues Gericht aus dem Formular hinzu */
    private void addMenuItem() {
        try {
            double price = parsePrice();
            menuService.addMenuItem(
                    tfName.getText(),
                    (String) cbCategory.getSelectedItem(),
                    price,
                    tfDescription.getText(),
                    chkAvailable.isSelected()
            );
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Gericht erfolgreich hinzugefügt.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (NumberFormatException e) {
            showError("Bitte einen gültigen Preis eingeben (z.B. 12.50).");
        }
    }

    /** Aktualisiert das ausgewählte Gericht */
    private void updateMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { showError("Bitte ein Gericht in der Tabelle auswählen."); return; }

        try {
            int id = (int) tableModel.getValueAt(row, 0);
            double price = parsePrice();

            MenuItem item = new MenuItem(id,
                    tfName.getText(),
                    (String) cbCategory.getSelectedItem(),
                    price,
                    tfDescription.getText(),
                    chkAvailable.isSelected()
            );
            menuService.updateMenuItem(item);
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Gericht erfolgreich aktualisiert.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (NumberFormatException e) {
            showError("Bitte einen gültigen Preis eingeben (z.B. 12.50).");
        }
    }

    /** Löscht das ausgewählte Gericht nach Bestätigung */
    private void deleteMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { showError("Bitte ein Gericht auswählen."); return; }

        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "\"" + name + "\" wirklich löschen?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            menuService.deleteMenuItem(id);
            loadData();
            clearForm();
        }
    }

    /** Befüllt das Formular aus der ausgewählten Tabellenzeile */
    private void fillFormFromTable() {
        int row = menuTable.getSelectedRow();
        if (row < 0) return;

        tfName.setText((String) tableModel.getValueAt(row, 1));
        cbCategory.setSelectedItem(tableModel.getValueAt(row, 2));
        tfPrice.setText(tableModel.getValueAt(row, 3).toString().replace(",", "."));
        tfDescription.setText((String) tableModel.getValueAt(row, 4));
        chkAvailable.setSelected("Ja".equals(tableModel.getValueAt(row, 5)));
    }

    /** Leert das Eingabeformular */
    private void clearForm() {
        tfName.setText("");
        tfPrice.setText("");
        tfDescription.setText("");
        cbCategory.setSelectedIndex(0);
        chkAvailable.setSelected(true);
        menuTable.clearSelection();
    }

    /** Parst den Preis aus dem Textfeld (akzeptiert Komma und Punkt) */
    private double parsePrice() throws NumberFormatException {
        return Double.parseDouble(tfPrice.getText().trim().replace(",", "."));
    }

    /** Zeigt eine Fehlermeldung an */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    /** Erstellt einen gestylten Button */
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 35));
        return btn;
    }

    /** Refreshable: Daten neu laden bei Tab-Wechsel */
    @Override
    public void refresh() {
        loadData();
    }
}
