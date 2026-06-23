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

    // ---- GUI-Komponenten ----
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarButton[] navButtons;

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
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        menuPanel        = new MenuPanel();
        orderPanel       = new OrderPanel();
        customerPanel    = new CustomerPanel();
        reservationPanel = new ReservationPanel();
        invoicePanel     = new InvoicePanel();
        statisticsPanel  = new StatisticsPanel();
        inventoryPanel   = new InventoryPanel();
        expensePanel     = new ExpensePanel();
    }

    /** Fügt die Panels zum Sidebar und CardLayout hinzu */
    private void layoutComponents() {
        String[] titles = {
            "Speisekarte",
            "Bestellungen",
            "Kunden",
            "Reservierungen",
            "Rechnungen",
            "Statistiken",
            "Inventar",
            "Ausgaben"
        };
        String[] displayTitles = {
            "📋   Speisekarte",
            "🛒   Bestellungen",
            "👤   Kunden",
            "📅   Reservierungen",
            "🧾   Rechnungen",
            "📊   Statistiken",
            "📦   Inventar",
            "💸   Ausgaben"
        };
        Component[] panels = {
            menuPanel, orderPanel, customerPanel,
            reservationPanel, invoicePanel, statisticsPanel,
            inventoryPanel, expensePanel
        };

        // Left Sidebar Container
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(30, 38, 64)); // #1e2640
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Brand Logo Panel
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(new Color(30, 38, 64));
        brandPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 25)));
        
        JLabel brandLabel = new JLabel("🍽   Restaurant System");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        brandPanel.add(brandLabel, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(30, 38, 64));
        navPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        navButtons = new SidebarButton[titles.length];
        
        // Page Header Label (final so it can be updated inside action listeners)
        final JLabel pageTitleLabel = new JLabel("Speisekarte");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitleLabel.setForeground(new Color(33, 37, 41)); // #212529

        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            final String cardName = titles[i];
            
            // Add panel to CardLayout container
            contentPanel.add(panels[i], cardName);
            
            SidebarButton btn = new SidebarButton(displayTitles[i]);
            navButtons[i] = btn;
            navPanel.add(btn);
            
            // Fixed button height/spacing
            navPanel.add(Box.createRigidArea(new Dimension(0, 2)));

            btn.addActionListener(e -> {
                // Switch Card
                cardLayout.show(contentPanel, cardName);
                
                // Update button active state colors
                for (int j = 0; j < navButtons.length; j++) {
                    navButtons[j].setActive(j == index);
                }
                
                // Update page title
                pageTitleLabel.setText(cardName);
                
                // Refresh data if Panel is Refreshable
                Component activePanel = panels[index];
                if (activePanel instanceof Refreshable) {
                    ((Refreshable) activePanel).refresh();
                }
            });
        }
        navPanel.add(Box.createVerticalGlue());
        sidebar.add(navPanel, BorderLayout.CENTER);

        // Sidebar Footer Panel with Logout Button styled like other buttons
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(30, 38, 64));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 25)), // separator line
            BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        SidebarButton btnLogout = new SidebarButton("🚪   Abmelden");
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
        footerPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        // Main Workspace container on the right
        JPanel mainWorkspace = new JPanel(new BorderLayout());
        mainWorkspace.setBackground(new Color(248, 249, 255));

        // Page Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)), // bottom line #dee2e6
            BorderFactory.createEmptyBorder(15, 24, 15, 24)
        ));

        // Fetch user and role
        de.restaurant.model.User currentUser = de.restaurant.service.AuthService.getCurrentUser();
        String userText = "👤   Eingeloggt als: Gast";
        if (currentUser != null) {
            userText = "👤   Eingeloggt als: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")";
        }
        JLabel userInfoLabel = new JLabel(userText);
        userInfoLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        userInfoLabel.setForeground(new Color(108, 117, 125)); // #6c757d

        headerPanel.add(pageTitleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoLabel, BorderLayout.EAST);
        
        mainWorkspace.add(headerPanel, BorderLayout.NORTH);
        mainWorkspace.add(contentPanel, BorderLayout.CENTER);

        // Add Sidebar and Main Workspace to Frame
        add(sidebar, BorderLayout.WEST);
        add(mainWorkspace, BorderLayout.CENTER);

        // Select Speisekarte by default
        if (navButtons.length > 0) {
            navButtons[0].setActive(true);
            cardLayout.show(contentPanel, titles[0]);
            pageTitleLabel.setText(titles[0]);
            // Refresh Speisekarte
            menuPanel.refresh();
        }
    }

    /** Wendet Design-Styles auf das Fenster an */
    private void applyStyles() {
        getContentPane().setBackground(new Color(248, 249, 255));
    }

    /** Custom Button component for Sidebar Navigation */
    private static class SidebarButton extends JButton {
        private boolean active = false;
        private static final Color BG_HOVER = new Color(42, 52, 84); // #2a3454
        private static final Color BG_ACTIVE = new Color(59, 72, 112); // #3b4870
        private static final Color ACCENT = new Color(13, 110, 253); // #0d6efd
        private static final Color TEXT_COLOR = new Color(184, 193, 236); // #b8c1ec
        private static final Color TEXT_ACTIVE = Color.WHITE;

        public SidebarButton(String text) {
            super(text);
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            setForeground(TEXT_COLOR);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            // Limit width, pad text
            setMaximumSize(new Dimension(240, 48));
            setPreferredSize(new Dimension(240, 48));
            setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

            // Hover effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (!active) {
                        setBackground(BG_HOVER);
                        setForeground(TEXT_ACTIVE);
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (!active) {
                        setBackground(null);
                        setForeground(TEXT_COLOR);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(BG_ACTIVE);
                setForeground(TEXT_ACTIVE);
            } else {
                setBackground(null);
                setForeground(TEXT_COLOR);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            if (active) {
                g2.setColor(BG_ACTIVE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Left accent border
                g2.setColor(ACCENT);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (getBackground() != null) {
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
