package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.MenuItem;
import de.restaurant.service.MenuService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
        setLayout(new BorderLayout(10, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        buildTable();
        buildForm();
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

        // Alignments: Left-align textual columns, right-align Price (index 3)
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        menuTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        menuTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        menuTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        menuTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        menuTable.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);
        menuTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Zeile auswählen → Formular befüllen
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Modern styled container for table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("Aktuelle Speisekarte");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        tableContainer.add(titleLabel, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    /** Erstellt das Eingabeformular und die Buttons im unteren Bereich */
    private void buildForm() {
        JPanel formPanel = new JPanel(new BorderLayout(0, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Gericht bearbeiten / hinzufügen");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(33, 37, 41));
        formPanel.add(titleLabel, BorderLayout.NORTH);

        // Grid container for horizontal inputs
        JPanel formGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        formGrid.setBackground(Color.WHITE);

        // Soft gray border for inputs
        javax.swing.border.Border softBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );

        // Name
        tfName = new JTextField();
        tfName.setBorder(softBorder);
        tfName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(createFormGroup("Name *", tfName));

        // Kategorie
        cbCategory = new JComboBox<>(MenuService.CATEGORIES);
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230), 1));
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(createFormGroup("Kategorie *", cbCategory));

        // Preis
        tfPrice = new JTextField();
        tfPrice.setBorder(softBorder);
        tfPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(createFormGroup("Preis (€) *", tfPrice));

        // Beschreibung
        tfDescription = new JTextField();
        tfDescription.setBorder(softBorder);
        tfDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(createFormGroup("Beschreibung", tfDescription));

        // Verfügbar (Checkbox)
        chkAvailable = new JCheckBox();
        chkAvailable.setSelected(true);
        chkAvailable.setBackground(Color.WHITE);
        chkAvailable.setFocusPainted(false);
        JPanel chkWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        chkWrapper.setBackground(Color.WHITE);
        chkWrapper.add(chkAvailable);
        formGrid.add(createFormGroup("Verfügbar", chkWrapper));

        formPanel.add(formGrid, BorderLayout.CENTER);

        // Create buttons
        btnAdd    = createButton("💾 Speichern / Hinzufügen", new Color(13, 110, 253));
        btnClear  = createButton("✖ Leeren", new Color(110, 120, 129));
        btnDelete = createButton("🗑 Löschen", new Color(220, 53, 69));
        btnUpdate = new JButton(); // unused dummy to avoid null references

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnAdd);
        btnPanel.add(btnClear);
        btnPanel.add(btnDelete);

        formPanel.add(btnPanel, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnAdd.addActionListener(e -> {
            int row = menuTable.getSelectedRow();
            if (row >= 0) {
                updateMenuItem();
            } else {
                addMenuItem();
            }
        });
        btnClear.addActionListener(e -> clearForm());
        btnDelete.addActionListener(e -> deleteMenuItem());
    }

    /** Helper method to create stacked form group */
    private JPanel createFormGroup(String labelText, Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(108, 117, 125)); // #6c757d
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
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

    /** Erstellt einen gestylten Button mit Hover-Effekt */
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 36));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /** Refreshable: Daten neu laden bei Tab-Wechsel */
    @Override
    public void refresh() {
        loadData();
    }
}
