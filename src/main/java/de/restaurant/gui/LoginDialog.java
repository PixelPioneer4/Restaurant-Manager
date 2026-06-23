package de.restaurant.gui;

import de.restaurant.exception.ValidationException;
import de.restaurant.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modaler Anmeldedialog, der vor dem Hauptfenster angezeigt wird.
 */
public class LoginDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private final JTextField     txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel         lblError;
    private boolean loginSuccessful = false;

    /**
     * Konstruktor
     * @param parent Das übergeordnete Fenster (Frame)
     */
    public LoginDialog(Frame parent) {
        super(parent, "🍽 Restaurant System — Anmelden", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 280);
        setResizable(false);
        setLocationRelativeTo(parent);

        // Haupt-Panel mit Abständen
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 249, 255));

        // Titel-Bereich oben
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Restaurant System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 45, 90)); // Dunkelblau
        
        JLabel lblSub = new JLabel("Bitte melden Sie sich an");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 110, 130));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        headerPanel.add(lblTitle, gbc);
        gbc.gridy = 1;
        headerPanel.add(lblSub, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Eingabe-Felder in der Mitte
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.fill = GridBagConstraints.HORIZONTAL;
        fg.insets = new Insets(6, 6, 6, 6);

        JLabel lblUser = new JLabel("Benutzername:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblPass = new JLabel("Passwort:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Fehlermeldungs-Label in rot
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(Color.RED);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        fg.gridx = 0; fg.gridy = 0; fg.weightx = 0.3;
        fieldsPanel.add(lblUser, fg);
        fg.gridx = 1; fg.weightx = 0.7;
        fieldsPanel.add(txtUsername, fg);

        fg.gridx = 0; fg.gridy = 1; fg.weightx = 0.3;
        fieldsPanel.add(lblPass, fg);
        fg.gridx = 1; fg.weightx = 0.7;
        fieldsPanel.add(txtPassword, fg);

        fg.gridx = 0; fg.gridy = 2; fg.gridwidth = 2; fg.weightx = 1.0;
        fieldsPanel.add(lblError, fg);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Button-Leiste unten
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnLogin = new JButton("Anmelden");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogin.setBackground(new Color(30, 45, 90)); // Dunkelblau
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnExit = new JButton("Beenden");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setBackground(new Color(120, 120, 120)); // Grau
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Event-Handler
        btnLogin.addActionListener(e -> performLogin());
        btnExit.addActionListener(e -> {
            loginSuccessful = false;
            dispose();
        });

        // Enter im Passwort-Feld führt Login aus
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // Enter im Benutzernamen-Feld springt zum Passwort-Feld
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocusInWindow();
                }
            }
        });
    }

    /** Führt den Anmeldevorgang aus */
    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        try {
            authService.login(username, password);
            loginSuccessful = true;
            dispose();
        } catch (ValidationException e) {
            lblError.setText(e.getMessage());
            loginSuccessful = false;
        }
    }

    /** Gibt zurück, ob die Anmeldung erfolgreich war */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
