package de.restaurant.gui;

import de.restaurant.gui.panels.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Hauptfenster der Restaurant-Verwaltungsanwendung.
 * Enthält eine Tab-Navigation mit allen Verwaltungsmodulen.
 * Tabs sind doppelt so groß und haben individuelle Farben.
 */
public class MainFrame extends JFrame {

    // ---- Tab-Farben (je ein warmer, unverwechselbarer Ton) ----
    private static final Color[] TAB_COLORS = {
        new Color(52, 120, 180),   // 📋 Speisekarte  – Stahlblau
        new Color(210, 95,  45),   // 🛒 Bestellungen – Burnt Orange
        new Color(60, 150,  80),   // 👤 Kunden       – Waldgrün
        new Color(140,  60, 170),  // 📅 Reservierung – Violett
        new Color(190, 140,  20),  // 🧾 Rechnungen   – Gold
        new Color(40,  160, 160),  // 📊 Statistiken  – Petrol
        new Color(180, 80,  120),  // 📦 Inventar     – Pink/Weinrot
        new Color(150, 40,   40),  // 💸 Ausgaben     – Dunkelrot
    };

    private static final Color TAB_TEXT       = Color.WHITE;
    private static final Color TAB_SELECTED_BORDER = new Color(255, 255, 255, 80);

    // ---- GUI-Komponenten ----
    private JTabbedPane tabbedPane;

    // ---- Panel-Instanzen ----
    private MenuPanel        menuPanel;
    private OrderPanel       orderPanel;
    private CustomerPanel    customerPanel;
    private ReservationPanel reservationPanel;
    private InvoicePanel     invoicePanel;
    private StatisticsPanel  statisticsPanel;
    private InventoryPanel   inventoryPanel;
    private ExpensePanel     expensePanel;

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
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

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
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        menuPanel        = new MenuPanel();
        orderPanel       = new OrderPanel();
        customerPanel    = new CustomerPanel();
        reservationPanel = new ReservationPanel();
        invoicePanel     = new InvoicePanel();
        statisticsPanel  = new StatisticsPanel();
        inventoryPanel   = new InventoryPanel();
        expensePanel     = new ExpensePanel();
    }

    /** Fügt die Panels zum TabbedPane hinzu und setzt individuelle Tab-Renderer */
    private void layoutComponents() {
        String[] titles = {
            "📋  Speisekarte",
            "🛒  Bestellungen",
            "👤  Kunden",
            "📅  Reservierungen",
            "🧾  Rechnungen",
            "📊  Statistiken",
            "📦  Inventar",
            "💸  Ausgaben"
        };
        Component[] panels = {
            menuPanel, orderPanel, customerPanel,
            reservationPanel, invoicePanel, statisticsPanel,
            inventoryPanel, expensePanel
        };

        for (int i = 0; i < titles.length; i++) {
            tabbedPane.addTab(titles[i], panels[i]);
            // Individuellen farbigen Tab-Renderer setzen
            tabbedPane.setTabComponentAt(i, createTabLabel(titles[i], TAB_COLORS[i]));
        }

        // Tab-Wechsel → Daten aktualisieren
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof Refreshable) {
                ((Refreshable) selected).refresh();
            }
            // Alle Tab-Labels neu zeichnen (Highlight für aktiven Tab)
            repaintTabs();
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Statusleiste mit Abmelden-Button
        JPanel southPanel = new JPanel(new BorderLayout(5, 0));
        southPanel.setBorder(BorderFactory.createEtchedBorder());
        southPanel.setBackground(new Color(240, 240, 245));

        JLabel statusBar = new JLabel("  Restaurant Management System v1.0  |  Bereit");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JButton btnLogout = new JButton("Abmelden");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Wirklich abmelden?",
                    "Abmelden",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                de.restaurant.service.AuthService.logout();
                
                // Login-Dialog erneut anzeigen
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                if (login.isLoginSuccessful()) {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });

        southPanel.add(statusBar, BorderLayout.WEST);
        southPanel.add(btnLogout, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Erstellt ein individuell gefärbtes Tab-Label.
     * Doppelte Höhe durch großen Font + vertikales Padding.
     *
     * @param title Text des Tabs
     * @param color Hintergrundfarbe
     * @return Panel das als Tab-Komponente verwendet wird
     */
    private JPanel createTabLabel(String title, Color color) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Prüfen ob dieser Tab gerade aktiv ist
                int idx = tabbedPane.indexOfTabComponent(this);
                boolean selected = (tabbedPane.getSelectedIndex() == idx);

                // Hintergrund (etwas heller wenn selektiert)
                Color bg = selected ? color.brighter() : color;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 6, 10, 10);

                // Heller Glanz-Streifen oben
                g2.setColor(new Color(255, 255, 255, selected ? 60 : 30));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 8, 8);

                // Unterer Rand-Highlight wenn selektiert
                if (selected) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(4, getHeight() - 1, getWidth() - 4, getHeight() - 1);
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Label mit großem Font (doppelt so groß wie vorher → 20pt statt ~10pt)
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        label.setForeground(TAB_TEXT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18)); // viel Padding = doppelte Höhe

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    /** Zeichnet alle Tab-Labels neu (damit Selektion sichtbar wird) */
    private void repaintTabs() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tc = tabbedPane.getTabComponentAt(i);
            if (tc != null) tc.repaint();
        }
    }

    /** Wendet Design-Styles auf das Fenster an */
    private void applyStyles() {
        getContentPane().setBackground(new Color(245, 245, 250));

        // TabbedPane selbst transparent/minimal stylen
        tabbedPane.setBackground(new Color(230, 232, 240));
        tabbedPane.setOpaque(true);

        // Tab-Leiste etwas höher für bessere Optik (via UI)
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabHeight(int placement, int index, int fontHeight) {
                return 54; // Feste Höhe der Tab-Leiste (doppelt von Standard ~26)
            }
            @Override
            protected void paintTabBackground(Graphics g, int placement, int index,
                                              int x, int y, int w, int h, boolean isSelected) {
                // Hintergrund nicht vom L&F malen – das macht unser custom Label
            }
            @Override
            protected void paintTabBorder(Graphics g, int placement, int index,
                                          int x, int y, int w, int h, boolean isSelected) {
                // Kein Standard-Rahmen
            }
            @Override
            protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] rects,
                                               int tabIndex, Rectangle iconRect,
                                               Rectangle textRect, boolean isSelected) {
                // Kein Fokus-Rahmen
            }
        });
    }
}
