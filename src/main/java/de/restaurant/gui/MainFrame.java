package de.restaurant.gui;

import de.restaurant.gui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Hauptfenster der Restaurant-Verwaltungsanwendung.
 * Enthält eine Tab-Navigation mit allen Verwaltungsmodulen.
 */
public class MainFrame extends JFrame {

    // ---- GUI-Komponenten ----
    private JTabbedPane tabbedPane;

    // ---- Panel-Instanzen ----
    private MenuPanel       menuPanel;
    private OrderPanel      orderPanel;
    private CustomerPanel   customerPanel;
    private ReservationPanel reservationPanel;
    private InvoicePanel    invoicePanel;
    private StatisticsPanel statisticsPanel;

    /** Konstruktor: Erstellt und konfiguriert das Hauptfenster */
    public MainFrame() {
        initializeFrame();
        initializeComponents();
        layoutComponents();
        applyStyles();
    }

    // ---- Initialisierung ----

    /** Konfiguriert das JFrame (Titel, Größe, Schließverhalten) */
    private void initializeFrame() {
        setTitle("🍽 Restaurant Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null); // Fenster in der Bildschirmmitte

        // Datenbankverbindung beim Schließen trennen
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Möchten Sie die Anwendung wirklich beenden?",
                        "Beenden bestätigen",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    de.restaurant.dao.DatabaseConnection.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });
    }

    /** Erstellt alle GUI-Komponenten */
    private void initializeComponents() {
        // Tabs erstellen
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Alle Module als Panels erstellen
        menuPanel        = new MenuPanel();
        orderPanel       = new OrderPanel();
        customerPanel    = new CustomerPanel();
        reservationPanel = new ReservationPanel();
        invoicePanel     = new InvoicePanel();
        statisticsPanel  = new StatisticsPanel();
    }

    /** Fügt die Panels zum TabbedPane hinzu */
    private void layoutComponents() {
        tabbedPane.addTab("📋  Speisekarte",    menuPanel);
        tabbedPane.addTab("🛒  Bestellungen",   orderPanel);
        tabbedPane.addTab("👤  Kunden",         customerPanel);
        tabbedPane.addTab("📅  Reservierungen", reservationPanel);
        tabbedPane.addTab("🧾  Rechnungen",     invoicePanel);
        tabbedPane.addTab("📊  Statistiken",    statisticsPanel);

        // Beim Wechseln des Tabs: Daten aktualisieren
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof Refreshable) {
                ((Refreshable) selected).refresh();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Statusleiste am unteren Rand
        JLabel statusBar = new JLabel("  Restaurant Management System v1.0  |  Bereit");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        add(statusBar, BorderLayout.SOUTH);
    }

    /** Wendet Design-Styles auf das Fenster an */
    private void applyStyles() {
        // Hintergrundfarbe
        getContentPane().setBackground(new Color(245, 245, 250));

        // Tab-Farben
        tabbedPane.setBackground(new Color(255, 255, 255));
        tabbedPane.setForeground(new Color(50, 50, 80));
    }
}
