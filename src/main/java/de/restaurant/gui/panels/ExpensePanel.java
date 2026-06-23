package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Expense;
import de.restaurant.service.ExpenseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * GUI-Panel für Ausgabenverwaltung und GuV-Übersicht.
 */
public class ExpensePanel extends JPanel implements Refreshable {

    private final ExpenseService expenseService;

    // Tabelle
    private JTable table;
    private DefaultTableModel tableModel;

    // Formular-Felder
    private JComboBox<String> cbCategory;
    private JTextField txtAmount, txtDate, txtDescription;

    // GuV-Labels
    private JLabel lblTotalRevenue, lblTotalExpenses, lblNetProfit;
    private JPanel pnlProfitCard;

    private static final String[] CATEGORIES = {"PERSONAL", "MIETE", "LIEFERANT", "SONSTIGES"};

    public ExpensePanel() {
        this.expenseService = new ExpenseService();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        // ---- Tabelle ----
        String[] cols = {"ID", "Kategorie", "Betrag", "Datum", "Beschreibung"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(220, 220, 235));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(120);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Ausgabenübersicht"));
        add(sp, BorderLayout.CENTER);

        // ---- Rechte Seite ----
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(new Color(248, 249, 255));

        // Ausgaben buchen
        rightPanel.add(createSectionLabel("➕ Neue Ausgabe buchen"));
        
        rightPanel.add(new JLabel("Kategorie:"));
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        rightPanel.add(cbCategory);
        rightPanel.add(Box.createVerticalStrut(6));

        txtAmount = addField(rightPanel, "Betrag (€) *:");
        txtDate = addField(rightPanel, "Datum (JJJJ-MM-TT) *:");
        txtDate.setText(LocalDate.now().toString());
        txtDescription = addField(rightPanel, "Beschreibung:");

        JButton btnAdd = new JButton("Ausgabe buchen");
        btnAdd.setBackground(new Color(180, 50, 50)); // Dunkelrot für Ausgaben
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnAdd.addActionListener(e -> addExpense());
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(btnAdd);

        rightPanel.add(Box.createVerticalStrut(15));

        // Löschen-Button
        JButton btnDelete = new JButton("🗑 Ausgabe stornieren/löschen");
        btnDelete.setBackground(new Color(100, 100, 110));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnDelete.addActionListener(e -> deleteExpense());
        rightPanel.add(btnDelete);

        rightPanel.add(Box.createVerticalStrut(20));

        // Gewinn & Verlust (GuV) Card
        rightPanel.add(createSectionLabel("📊 Gewinn- & Verlustrechnung"));
        
        pnlProfitCard = new JPanel();
        pnlProfitCard.setLayout(new GridLayout(3, 1, 0, 10));
        pnlProfitCard.setBackground(Color.WHITE);
        pnlProfitCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        lblTotalRevenue = new JLabel("Gesamtumsatz: 0,00 €");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalExpenses = new JLabel("Gesamtausgaben: 0,00 €");
        lblTotalExpenses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalExpenses.setForeground(new Color(180, 50, 50));
        
        lblNetProfit = new JLabel("Nettogewinn: 0,00 €");
        lblNetProfit.setFont(new Font("Segoe UI", Font.BOLD, 15));

        pnlProfitCard.add(lblTotalRevenue);
        pnlProfitCard.add(lblTotalExpenses);
        pnlProfitCard.add(lblNetProfit);
        rightPanel.add(pnlProfitCard);

        add(rightPanel, BorderLayout.EAST);

        // ---- Titel oben ----
        JLabel title = new JLabel("🧾 Finanzen & Ausgabenverwaltung");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
    }

    private void loadData() {
        // Tabelle laden
        tableModel.setRowCount(0);
        List<Expense> expenses = expenseService.getAllExpenses();
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                e.getId(),
                e.getCategory(),
                String.format("%.2f €", e.getAmount()),
                e.getExpenseDate(),
                e.getDescription()
            });
        }

        // GuV berechnen
        double totalRev = expenseService.getTotalRevenue();
        double totalExp = expenseService.getTotalExpenses();
        double netProfit = expenseService.getNetProfit();

        lblTotalRevenue.setText(String.format("Gesamtumsatz: %.2f €", totalRev));
        lblTotalExpenses.setText(String.format("Gesamtausgaben: %.2f €", totalExp));
        lblNetProfit.setText(String.format("Reingewinn: %.2f €", netProfit));

        // Farbe des Reingewinns anpassen
        if (netProfit >= 0) {
            lblNetProfit.setForeground(new Color(46, 139, 87)); // Grün
            pnlProfitCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(46, 139, 87), 2),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        } else {
            lblNetProfit.setForeground(new Color(178, 34, 34)); // Rot
            pnlProfitCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(178, 34, 34), 2),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        }
    }

    private void addExpense() {
        try {
            String category = (String) cbCategory.getSelectedItem();
            double amount = Double.parseDouble(txtAmount.getText().trim());
            String date = txtDate.getText().trim();
            String description = txtDescription.getText().trim();

            expenseService.addExpense(category, amount, date, description);
            
            // Felder zurücksetzen (außer Datum)
            txtAmount.setText("");
            txtDescription.setText("");
            
            loadData();
            JOptionPane.showMessageDialog(this, "Ausgabe erfolgreich gebucht!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Bitte einen gültigen Betrag eingeben.", "Fehler", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteExpense() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Bitte eine Ausgabe aus der Tabelle auswählen.", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String cat = (String) tableModel.getValueAt(row, 1);
        String amountStr = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Möchten Sie die Ausgabe #" + id + " (" + cat + " über " + amountStr + ") wirklich löschen?",
            "Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            expenseService.deleteExpense(id);
            loadData();
        }
    }

    private JTextField addField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(field);
        panel.add(Box.createVerticalStrut(4));
        return field;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    @Override
    public void refresh() {
        loadData();
        txtDate.setText(LocalDate.now().toString());
    }
}
