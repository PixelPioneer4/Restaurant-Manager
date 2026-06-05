package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Invoice;
import de.restaurant.model.Order;
import de.restaurant.service.InvoiceService;
import de.restaurant.service.OrderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI-Panel für die Rechnungsverwaltung.
 * Ermöglicht das Erstellen von Rechnungen aus Bestellungen und das Verwalten des Bezahlstatus.
 */
public class InvoicePanel extends JPanel implements Refreshable {

    // ---- Services ----
    private final InvoiceService invoiceService = new InvoiceService();
    private final OrderService   orderService   = new OrderService();

    // ---- Rechnungstabelle ----
    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;

    // ---- Bestellungstabelle (zum Auswählen) ----
    private JTable orderTable;
    private DefaultTableModel orderTableModel;

    // ---- Buttons ----
    private JButton btnCreateInvoice, btnMarkPaid;

    // ---- Detail-Labels ----
    private JLabel lblInvoiceId, lblTotal, lblNet, lblTax, lblStatus, lblDate;

    private static final String[] INVOICE_COLS = {
        "Rech.-Nr.", "Best.-Nr.", "Datum", "Netto (€)", "MwSt. 19% (€)", "Gesamt (€)", "Status"
    };
    private static final String[] ORDER_COLS = {
        "ID", "Tisch", "Datum", "Status", "Betrag (€)"
    };

    /** Konstruktor */
    public InvoicePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        buildInvoiceTable();
        buildBottomPanel();
        loadData();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die Rechnungstabelle oben */
    private void buildInvoiceTable() {
        invoiceTableModel = new DefaultTableModel(INVOICE_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(28);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setGridColor(new Color(220, 220, 235));

        // Klick auf Rechnung → Details anzeigen
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showInvoiceDetails();
        });

        JScrollPane sp = new JScrollPane(invoiceTable);
        sp.setBorder(BorderFactory.createTitledBorder("Alle Rechnungen"));
        add(sp, BorderLayout.CENTER);
    }

    /** Erstellt das untere Panel (Bestellungen + Aktionen + Details) */
    private void buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(new Color(248, 249, 255));

        // Bestellungen-Tabelle (für Rechnungserstellung)
        orderTableModel = new DefaultTableModel(ORDER_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(orderTableModel);
        orderTable.setRowHeight(24);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        orderTable.setGridColor(new Color(220, 220, 235));

        JScrollPane orderSP = new JScrollPane(orderTable);
        orderSP.setBorder(BorderFactory.createTitledBorder("Bestellungen – Rechnung erstellen"));
        orderSP.setPreferredSize(new Dimension(0, 180));
        bottomPanel.add(orderSP, BorderLayout.CENTER);

        // Aktions-Panel rechts
        JPanel actionPanel = new JPanel(new BorderLayout(0, 10));
        actionPanel.setBackground(new Color(248, 249, 255));
        actionPanel.setPreferredSize(new Dimension(260, 0));

        // Detail-Box
        JPanel detailPanel = buildDetailPanel();
        actionPanel.add(detailPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        btnPanel.setBackground(new Color(248, 249, 255));
        btnPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        btnCreateInvoice = createButton("🧾 Rechnung erstellen", new Color(46, 139, 87));
        btnMarkPaid      = createButton("✅ Als bezahlt markieren", new Color(70, 130, 180));

        btnPanel.add(btnCreateInvoice);
        btnPanel.add(btnMarkPaid);
        actionPanel.add(btnPanel, BorderLayout.SOUTH);

        bottomPanel.add(actionPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event-Handler
        btnCreateInvoice.addActionListener(e -> createInvoice());
        btnMarkPaid.addActionListener(e      -> markPaid());
    }

    /** Erstellt das Rechnungsdetail-Panel */
    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Rechnungsdetails"));
        panel.setPreferredSize(new Dimension(0, 130));

        Font lbl = new Font("Segoe UI", Font.PLAIN, 12);
        Font val = new Font("Segoe UI", Font.BOLD, 12);

        lblInvoiceId = new JLabel("–");  lblInvoiceId.setFont(val);
        lblDate      = new JLabel("–");  lblDate.setFont(val);
        lblNet       = new JLabel("–");  lblNet.setFont(val);
        lblTax       = new JLabel("–");  lblTax.setFont(val);
        lblTotal     = new JLabel("–");  lblTotal.setFont(val);
        lblStatus    = new JLabel("–");  lblStatus.setFont(val);

        panel.add(label("Rechnungs-Nr.:", lbl)); panel.add(lblInvoiceId);
        panel.add(label("Datum:", lbl));          panel.add(lblDate);
        panel.add(label("Nettobetrag:", lbl));    panel.add(lblNet);
        panel.add(label("MwSt. (19%):", lbl));    panel.add(lblTax);
        panel.add(label("Gesamtbetrag:", lbl));   panel.add(lblTotal);
        panel.add(label("Status:", lbl));         panel.add(lblStatus);

        return panel;
    }

    private JLabel label(String text, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        return l;
    }

    // ---- Aktionen ----

    /** Lädt alle Rechnungen und offenen Bestellungen */
    private void loadData() {
        // Rechnungen laden
        invoiceTableModel.setRowCount(0);
        List<Invoice> invoices = invoiceService.getAllInvoices();
        for (Invoice inv : invoices) {
            invoiceTableModel.addRow(new Object[]{
                    inv.getId(),
                    inv.getOrder().getId(),
                    inv.getIssueDate().toString().replace("T", " ").substring(0, 16),
                    String.format("%.2f", inv.getNetAmount()),
                    String.format("%.2f", inv.getTaxAmount()),
                    String.format("%.2f", inv.getTotalAmount()),
                    inv.isPaid() ? "✅ Bezahlt" : "⏳ Offen"
            });
        }

        // Fertige Bestellungen ohne Rechnung laden
        orderTableModel.setRowCount(0);
        List<Order> orders = orderService.getAllOrders();
        for (Order o : orders) {
            if ("FERTIG".equals(o.getStatus())) {
                orderTableModel.addRow(new Object[]{
                        o.getId(),
                        o.getTableNumber(),
                        o.getOrderDate().toString().replace("T", " ").substring(0, 16),
                        o.getStatus(),
                        String.format("%.2f", o.getTotalAmount())
                });
            }
        }
    }

    /** Erstellt eine Rechnung für die ausgewählte Bestellung */
    private void createInvoice() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { showError("Bitte eine Bestellung auswählen."); return; }

        try {
            int orderId = (int) orderTableModel.getValueAt(row, 0);
            Invoice inv = invoiceService.createInvoice(orderId);
            JOptionPane.showMessageDialog(this,
                    "Rechnung #" + inv.getId() + " über " + String.format("%.2f €", inv.getTotalAmount()) + " erstellt.",
                    "Rechnung erstellt", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /** Markiert die ausgewählte Rechnung als bezahlt */
    private void markPaid() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) { showError("Bitte eine Rechnung auswählen."); return; }

        int invoiceId = (int) invoiceTableModel.getValueAt(row, 0);
        invoiceService.markAsPaid(invoiceId);
        JOptionPane.showMessageDialog(this, "Rechnung als bezahlt markiert.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
        loadData();
    }

    /** Zeigt Details der ausgewählten Rechnung */
    private void showInvoiceDetails() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) return;

        lblInvoiceId.setText("# " + invoiceTableModel.getValueAt(row, 0));
        lblDate.setText((String) invoiceTableModel.getValueAt(row, 2));
        lblNet.setText(invoiceTableModel.getValueAt(row, 3) + " €");
        lblTax.setText(invoiceTableModel.getValueAt(row, 4) + " €");
        lblTotal.setText(invoiceTableModel.getValueAt(row, 5) + " €");
        lblStatus.setText((String) invoiceTableModel.getValueAt(row, 6));
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
        btn.setPreferredSize(new Dimension(220, 35));
        return btn;
    }

    @Override
    public void refresh() {
        loadData();
    }
}
