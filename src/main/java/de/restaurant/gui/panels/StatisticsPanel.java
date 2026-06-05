package de.restaurant.gui.panels;

import de.restaurant.service.InvoiceService;
import de.restaurant.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * GUI-Panel für Statistiken und Tagesumsatz (Bonus-Funktion).
 * Zeigt Umsatzdaten, Top-Gerichte und Bestellstatistiken.
 */
public class StatisticsPanel extends JPanel implements Refreshable {

    // ---- Services ----
    private final ReportService  reportService  = new ReportService();
    private final InvoiceService invoiceService = new InvoiceService();

    // ---- KPI-Labels (oben) ----
    private JLabel lblTodayRevenue, lblTotalRevenue, lblTotalOrders, lblTodayOrders;

    // ---- Tabellen ----
    private JTable revenueTable;
    private DefaultTableModel revenueModel;
    private JTable topItemTable;
    private DefaultTableModel topItemModel;
    private JTable orderCountTable;
    private DefaultTableModel orderCountModel;

    // ---- Datumsauswahl ----
    private JTextField tfDate;
    private JButton btnLoadDay;

    /** Konstruktor */
    public StatisticsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 246, 255));

        buildKPIPanel();
        buildTablesPanel();
        loadAllStats();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die KPI-Karten oben */
    private void buildKPIPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setBackground(new Color(245, 246, 255));
        kpiPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        lblTodayRevenue  = buildKpiCard("Tagesumsatz (heute)", "0,00 €", new Color(46, 139, 87));
        lblTotalRevenue  = buildKpiCard("Gesamtumsatz", "0,00 €", new Color(70, 130, 180));
        lblTodayOrders   = buildKpiCard("Bestellungen heute", "0", new Color(184, 100, 30));
        lblTotalOrders   = buildKpiCard("Bestellungen gesamt", "0", new Color(120, 50, 160));

        // Die KpiCard-Methode gibt das Value-Label zurück – wir fügen das Panel hinzu
        // Neu bauen mit Panel-Rückgabe
        kpiPanel.add(buildKpiCardPanel("Tagesumsatz (heute)", new Color(46, 139, 87),  lblTodayRevenue));
        kpiPanel.add(buildKpiCardPanel("Gesamtumsatz",        new Color(70, 130, 180), lblTotalRevenue));
        kpiPanel.add(buildKpiCardPanel("Bestellungen heute",  new Color(184, 100, 30), lblTodayOrders));
        kpiPanel.add(buildKpiCardPanel("Gesamt Bestellungen", new Color(120, 50, 160), lblTotalOrders));

        add(kpiPanel, BorderLayout.NORTH);
    }

    /**
     * Hilfsmethode: Erstellt ein KPI-Karten-Panel.
     * @param title Der Titel der Karte
     * @param color Die Akzentfarbe
     * @param valueLabel Das Label, das den Wert anzeigt
     * @return Das fertige Panel
     */
    private JPanel buildKpiCardPanel(String title, Color color, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(80, 80, 100));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Erstellt ein KPI-Label (Hilfsmethode für Feldinitialisierung) */
    private JLabel buildKpiCard(String title, String value, Color color) {
        JLabel lbl = new JLabel(value, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(color);
        return lbl;
    }

    /** Erstellt das Panel mit den Statistik-Tabellen */
    private void buildTablesPanel() {
        JPanel center = new JPanel(new GridLayout(1, 3, 15, 0));
        center.setBackground(new Color(245, 246, 255));

        // --- Tabelle 1: Umsatz letzte 7 Tage ---
        revenueModel = new DefaultTableModel(new String[]{"Datum", "Umsatz (€)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        revenueTable = buildStyledTable(revenueModel);
        JScrollPane sp1 = new JScrollPane(revenueTable);
        sp1.setBorder(BorderFactory.createTitledBorder("Umsatz – letzte 7 Tage"));
        center.add(sp1);

        // --- Tabelle 2: Top 5 Gerichte ---
        topItemModel = new DefaultTableModel(new String[]{"Gericht", "Bestellungen"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topItemTable = buildStyledTable(topItemModel);
        JScrollPane sp2 = new JScrollPane(topItemTable);
        sp2.setBorder(BorderFactory.createTitledBorder("🏆 Top 5 beliebteste Gerichte"));
        center.add(sp2);

        // --- Tabelle 3: Bestellanzahl letzte 7 Tage ---
        orderCountModel = new DefaultTableModel(new String[]{"Datum", "Bestellungen"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderCountTable = buildStyledTable(orderCountModel);
        JScrollPane sp3 = new JScrollPane(orderCountTable);
        sp3.setBorder(BorderFactory.createTitledBorder("Bestellungen – letzte 7 Tage"));
        center.add(sp3);

        add(center, BorderLayout.CENTER);

        // --- Tagesumsatz-Abfrage unten ---
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        dayPanel.setBackground(new Color(245, 246, 255));
        dayPanel.setBorder(BorderFactory.createTitledBorder("Tagesumsatz für bestimmtes Datum"));

        tfDate = new JTextField(LocalDate.now().toString(), 12);
        btnLoadDay = new JButton("Laden");
        btnLoadDay.setBackground(new Color(70, 130, 180));
        btnLoadDay.setForeground(Color.WHITE);
        btnLoadDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoadDay.setFocusPainted(false);
        btnLoadDay.setBorderPainted(false);

        JLabel lblSpecificDay = new JLabel("Umsatz: –");
        lblSpecificDay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSpecificDay.setForeground(new Color(46, 139, 87));

        dayPanel.add(new JLabel("Datum (JJJJ-MM-TT):"));
        dayPanel.add(tfDate);
        dayPanel.add(btnLoadDay);
        dayPanel.add(lblSpecificDay);

        btnLoadDay.addActionListener(e -> {
            try {
                String date = tfDate.getText().trim();
                double rev = reportService.getDailyRevenue(date);
                lblSpecificDay.setText(String.format("Umsatz: %.2f €", rev));
            } catch (Exception ex) {
                lblSpecificDay.setText("Ungültiges Datum!");
            }
        });

        add(dayPanel, BorderLayout.SOUTH);
    }

    /** Erstellt eine gestylte JTable */
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(220, 220, 235));
        return table;
    }

    // ---- Daten laden ----

    /** Lädt alle Statistiken und befüllt die KPI-Karten und Tabellen */
    private void loadAllStats() {
        String today = LocalDate.now().toString();

        // KPI-Werte
        double todayRev = reportService.getDailyRevenue(today);
        double totalRev = reportService.getTotalRevenue();
        int    totalOrd = reportService.getTotalOrderCount();
        int    todayOrd = reportService.getOrderCountLastDays(1).getOrDefault(today, 0);

        lblTodayRevenue.setText(String.format("%.2f €", todayRev));
        lblTotalRevenue.setText(String.format("%.2f €", totalRev));
        lblTotalOrders.setText(String.valueOf(totalOrd));
        lblTodayOrders.setText(String.valueOf(todayOrd));

        // Umsatz letzte 7 Tage
        revenueModel.setRowCount(0);
        Map<String, Double> revenue = reportService.getRevenueLastDays(7);
        revenue.forEach((date, rev) ->
            revenueModel.addRow(new Object[]{date, String.format("%.2f", rev)})
        );

        // Top 5 Gerichte
        topItemModel.setRowCount(0);
        Map<String, Integer> topItems = reportService.getTopMenuItems(5);
        topItems.forEach((name, qty) ->
            topItemModel.addRow(new Object[]{name, qty})
        );

        // Bestellanzahl letzte 7 Tage
        orderCountModel.setRowCount(0);
        Map<String, Integer> orderCounts = reportService.getOrderCountLastDays(7);
        orderCounts.forEach((date, cnt) ->
            orderCountModel.addRow(new Object[]{date, cnt})
        );
    }

    @Override
    public void refresh() {
        loadAllStats();
    }
}
