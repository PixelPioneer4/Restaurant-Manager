package de.restaurant.gui.panels;

import de.restaurant.model.Reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ein interaktiver Saalplan für das Restaurant.
 * Zeichnet Tische, Stühle, Bar und Wände.
 * Tische sind grün (verfügbar) oder rot (belegt) gefärbt.
 */
public class SaalplanPanel extends JPanel {

    // Liste aller Tische
    private final List<VisualTable> tables = new ArrayList<>();
    
    // Aktuelle Reservierungen an dem gewählten Tag
    private List<Reservation> reservations = new ArrayList<>();
    private LocalDate currentDate = LocalDate.now();

    // Ausgewählte Tischnummer
    private int selectedTableNumber = -1;
    
    // Callback bei Tischauswahl
    private TableSelectionListener selectionListener;

    // Farben
    private static final Color COLOR_BG = new Color(245, 246, 250);
    private static final Color COLOR_WALL = new Color(75, 75, 75);
    private static final Color COLOR_WINDOW = new Color(173, 216, 230);
    private static final Color COLOR_BAR = new Color(139, 90, 43);
    private static final Color COLOR_BAR_BG = new Color(222, 184, 135);
    
    private static final Color COLOR_FREE = new Color(46, 139, 87);      // Forest Green
    private static final Color COLOR_FREE_HOVER = new Color(60, 179, 113);
    private static final Color COLOR_BUSY = new Color(178, 34, 34);       // Firebrick Red
    private static final Color COLOR_BUSY_HOVER = new Color(220, 20, 60);
    
    private static final Color COLOR_SELECTED = new Color(30, 144, 255); // Dodger Blue
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(40, 40, 40);

    // Interface für Callbacks bei Klicks auf Tische
    public interface TableSelectionListener {
        void tableSelected(int tableNumber);
    }

    public SaalplanPanel() {
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(800, 520));
        setMinimumSize(new Dimension(800, 520));
        
        // Tische initialisieren
        initializeTables();

        // Mouse-Listener für Hover und Klicks
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        ToolTipManager.sharedInstance().setDismissDelay(10000); // 10 Sekunden Tooltip-Dauer
    }

    public void setTableSelectionListener(TableSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setReservations(List<Reservation> reservations, LocalDate date) {
        this.reservations = reservations;
        this.currentDate = date;
        repaint();
    }

    public int getSelectedTableNumber() {
        return selectedTableNumber;
    }

    public void setSelectedTableNumber(int number) {
        this.selectedTableNumber = number;
        repaint();
    }

    private void initializeTables() {
        // Tisch-Koordinaten passend zum Mockup-Design
        // Parameter: id, x, y, width, height, seats, isCircular, label
        
        // Linke Seite (Tische 1, 2, 3)
        tables.add(new VisualTable(1, 60, 60, 75, 55, 3, false, "Tisch 1"));
        tables.add(new VisualTable(2, 60, 175, 75, 55, 3, false, "Tisch 2"));
        tables.add(new VisualTable(3, 60, 290, 75, 55, 3, false, "Tisch 3"));
        
        // Untere linke Ecke (Tisch 13 - runder Tisch)
        tables.add(new VisualTable(13, 65, 395, 60, 60, 2, true, "Tisch 13"));

        // Mitte-Links (Tische 4, 5, 6)
        tables.add(new VisualTable(4, 185, 60, 75, 50, 2, false, "Tisch 4"));
        tables.add(new VisualTable(5, 185, 175, 75, 55, 3, false, "Tisch 5"));
        tables.add(new VisualTable(6, 185, 290, 75, 55, 3, false, "Tisch 6"));
        
        // Untere Mitte-Links (Tisch 7 - runder Tisch)
        tables.add(new VisualTable(7, 190, 395, 60, 60, 2, true, "Tisch 7"));

        // Mitte-Rechts (Tische 8, 9, 10)
        tables.add(new VisualTable(8, 310, 60, 75, 50, 2, false, "Tisch 8"));
        tables.add(new VisualTable(9, 310, 175, 75, 55, 3, false, "Tisch 9"));
        tables.add(new VisualTable(10, 315, 290, 60, 60, 2, true, "Tisch 10"));
        
        // Untere Mitte-Rechts (Tisch 17 - runder Tisch)
        tables.add(new VisualTable(17, 315, 395, 60, 60, 2, true, "Tisch 17"));

        // Rechte Mitte (Tische 11, 12, 18)
        tables.add(new VisualTable(11, 435, 60, 65, 65, 3, true, "Tisch 11"));
        tables.add(new VisualTable(12, 440, 175, 55, 55, 2, true, "Tisch 12"));
        tables.add(new VisualTable(18, 440, 290, 55, 55, 2, true, "Tisch 18"));

        // Rechte Wand - Nischen/Sofas (Tische 14, 15, 16)
        tables.add(new VisualTable(14, 660, 60, 75, 65, 4, false, "Tisch 14"));
        tables.add(new VisualTable(15, 660, 175, 75, 75, 4, true, "Tisch 15"));
        tables.add(new VisualTable(16, 660, 300, 75, 65, 4, false, "Tisch 16"));
        
        // Stehtische / Bar-Tische (Tische 19, 20)
        tables.add(new VisualTable(19, 440, 395, 50, 50, 2, true, "Tisch 19"));
        tables.add(new VisualTable(20, 560, 395, 50, 50, 2, true, "Tisch 20"));
    }

    private Reservation getReservationForTable(int tableNum) {
        for (Reservation r : reservations) {
            if (r.getTableNumber() == tableNum) {
                return r;
            }
        }
        return null;
    }

    private void handleMouseMoved(MouseEvent e) {
        boolean anyHovered = false;
        for (VisualTable table : tables) {
            boolean wasHovered = table.isHovered;
            table.isHovered = table.contains(e.getPoint());
            
            if (table.isHovered) {
                anyHovered = true;
                Reservation r = getReservationForTable(table.id);
                if (r != null) {
                    String customerName = (r.getCustomer() != null) ? r.getCustomer().getName() : "Laufkundschaft";
                    setToolTipText("<html><body style='font-family: Segoe UI; padding: 5px;'>"
                            + "<b>" + table.label + " (" + table.seats + " Plätze)</b><br/>"
                            + "<span style='color: red;'>● Belegt</span><br/>"
                            + "<b>Kunde:</b> " + customerName + "<br/>"
                            + "<b>Zeit:</b> " + r.getReservationTime() + " Uhr<br/>"
                            + (r.getNotes() != null && !r.getNotes().isEmpty() ? "<b>Notizen:</b> " + r.getNotes() : "")
                            + "</body></html>");
                } else {
                    setToolTipText("<html><body style='font-family: Segoe UI; padding: 5px;'>"
                            + "<b>" + table.label + " (" + table.seats + " Plätze)</b><br/>"
                            + "<span style='color: green;'>● Verfügbar</span>"
                            + "</body></html>");
                }
            }
            
            if (wasHovered != table.isHovered) {
                repaint();
            }
        }
        
        if (anyHovered) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
            setToolTipText(null);
        }
    }

    private void handleMousePressed(MouseEvent e) {
        for (VisualTable table : tables) {
            if (table.contains(e.getPoint())) {
                selectedTableNumber = table.id;
                repaint();
                if (selectionListener != null) {
                    selectionListener.tableSelected(table.id);
                }
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Hintergrund zeichnen
        g2.setColor(COLOR_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 2. Bar-Bereich zeichnen (Mockup Stil)
        drawBarArea(g2);

        // 3. Wände, Türen und Fenster zeichnen
        drawWallsAndOpenings(g2);

        // 4. Tische und Stühle zeichnen
        for (VisualTable table : tables) {
            Reservation res = getReservationForTable(table.id);
            boolean isBusy = (res != null);
            table.draw(g2, isBusy, selectedTableNumber == table.id);
        }

        // 5. Legende zeichnen
        drawLegend(g2);
        
        // Titel oben zentriert
        g2.setColor(COLOR_TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String title = "Saalplan (Interaktiv)";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 28);
    }

    private void drawWallsAndOpenings(Graphics2D g2) {
        // Außenwand Abmessungen
        int xMin = 20;
        int yMin = 35;
        int xMax = getWidth() - 25;
        int yMax = getHeight() - 55;
        
        g2.setColor(COLOR_WALL);
        g2.setStroke(new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // Wände zeichnen (mit Lücken für Türen und Fenster)
        // Top-Wand mit zwei Fenstern
        g2.drawLine(xMin, yMin, 250, yMin);
        g2.drawLine(350, yMin, 550, yMin);
        g2.drawLine(650, yMin, xMax, yMin);

        // Linke Wand mit einem Fenster
        g2.drawLine(xMin, yMin, xMin, 180);
        g2.drawLine(xMin, 280, xMin, yMax);

        // Rechte Wand
        g2.drawLine(xMax, yMin, xMax, yMax);

        // Untere Wand mit Eingangstür in der Mitte
        g2.drawLine(xMin, yMax, 340, yMax);
        g2.drawLine(440, yMax, xMax, yMax);

        // Fenster zeichnen (Hellblau)
        g2.setColor(COLOR_WINDOW);
        g2.setStroke(new BasicStroke(5));
        g2.drawLine(255, yMin, 345, yMin); // Top Fenster 1
        g2.drawLine(555, yMin, 645, yMin); // Top Fenster 2
        g2.drawLine(xMin, 185, xMin, 275); // Linkes Fenster

        // Eingangstüren zeichnen (unten Mitte)
        g2.setColor(COLOR_WALL);
        g2.setStroke(new BasicStroke(2));
        // Linker Türflügel
        g2.drawLine(340, yMax, 340, yMax - 30);
        g2.drawArc(340 - 30, yMax - 30, 60, 60, 0, 90);
        // Rechter Türflügel
        g2.drawLine(440, yMax, 440, yMax - 30);
        g2.drawArc(440 - 30, yMax - 30, 60, 60, 90, 90);
    }

    private void drawBarArea(Graphics2D g2) {
        // Zeichnet den braunen Bartresen
        g2.setColor(COLOR_BAR_BG);
        // L-Form füllen
        int[] xPoints = {540, 600, 600, 570, 570, 540};
        int[] yPoints = {150, 150, 350, 350, 180, 180};
        g2.fillPolygon(xPoints, yPoints, 6);
        
        g2.setColor(COLOR_BAR);
        g2.setStroke(new BasicStroke(3));
        g2.drawPolygon(xPoints, yPoints, 6);

        // Stühle an der Bar zeichnen (rote Kreise)
        g2.setColor(COLOR_BUSY);
        int stoolRadius = 14;
        // Vertikale Stuhl-Reihe
        for (int y = 205; y <= 335; y += 42) {
            g2.fillOval(518, y, stoolRadius, stoolRadius);
            g2.setColor(COLOR_TEXT_DARK);
            g2.drawOval(518, y, stoolRadius, stoolRadius);
            g2.setColor(COLOR_BUSY);
        }
        // Horizontale Stuhl-Reihe
        g2.fillOval(570, 362, stoolRadius, stoolRadius);
        g2.setColor(COLOR_TEXT_DARK);
        g2.drawOval(570, 362, stoolRadius, stoolRadius);

        // Label "Bar"
        g2.setColor(COLOR_TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.ITALIC | Font.BOLD, 14));
        g2.drawString("Bar", 555, 240);
    }

    private void drawLegend(Graphics2D g2) {
        int lx = getWidth() - 150;
        int ly = getHeight() - 40;
        
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(COLOR_TEXT_DARK);

        // Verfügbar
        g2.setColor(COLOR_FREE);
        g2.fillRect(lx, ly, 15, 15);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(lx, ly, 15, 15);
        g2.setColor(COLOR_TEXT_DARK);
        g2.drawString("Verfügbar", lx + 22, ly + 12);

        // Belegt
        g2.setColor(COLOR_BUSY);
        g2.fillRect(lx, ly + 20, 15, 15);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(lx, ly + 20, 15, 15);
        g2.setColor(COLOR_TEXT_DARK);
        g2.drawString("Belegt / Reserviert", lx + 22, ly + 32);
    }

    // Interne Hilfsklasse zur Repräsentation und Zeichnung eines Tisches
    private static class VisualTable {
        int id;
        int x, y, width, height;
        int seats;
        boolean isCircular;
        String label;
        boolean isHovered = false;

        VisualTable(int id, int x, int y, int width, int height, int seats, boolean isCircular, String label) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.seats = seats;
            this.isCircular = isCircular;
            this.label = label;
        }

        boolean contains(Point p) {
            if (isCircular) {
                Ellipse2D circle = new Ellipse2D.Double(x, y, width, height);
                return circle.contains(p);
            } else {
                RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
                return rect.contains(p);
            }
        }

        void draw(Graphics2D g2, boolean isBusy, boolean isSelected) {
            Color baseColor = isBusy ? (isHovered ? COLOR_BUSY_HOVER : COLOR_BUSY) 
                                     : (isHovered ? COLOR_FREE_HOVER : COLOR_FREE);
            
            // 1. Stühle zeichnen (Farbe passend zum Tisch-Status)
            g2.setColor(baseColor.darker());
            drawChairs(g2);

            // 2. Tischkörper zeichnen
            g2.setColor(baseColor);
            Shape shape;
            if (isCircular) {
                shape = new Ellipse2D.Double(x, y, width, height);
            } else {
                shape = new RoundRectangle2D.Double(x, y, width, height, 12, 12);
            }
            g2.fill(shape);

            // Rand / Auswahlrahmen
            if (isSelected) {
                g2.setColor(COLOR_SELECTED);
                g2.setStroke(new BasicStroke(4));
            } else {
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(1.5f));
            }
            g2.draw(shape);

            // 3. Text zeichnen
            g2.setColor(COLOR_TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            
            String line1 = label;
            String line2 = seats + " Plätze";
            
            int textX1 = x + (width - fm.stringWidth(line1)) / 2;
            int textY1 = y + (height / 2) - 2;
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(line1, textX1, textY1);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            int textX2 = x + (width - fm.stringWidth(line2)) / 2;
            int textY2 = y + (height / 2) + 12;
            g2.drawString(line2, textX2, textY2);
        }

        private void drawChairs(Graphics2D g2) {
            int chairSize = 12;
            int offset = 10;
            
            if (isCircular) {
                // Stühle kreisförmig um den Tisch zeichnen
                double cx = x + width / 2.0;
                double cy = y + height / 2.0;
                double r = (width / 2.0) + 7;
                
                for (int i = 0; i < seats; i++) {
                    double angle = i * (2 * Math.PI / seats) - Math.PI / 2;
                    int chairX = (int) (cx + Math.cos(angle) * r - chairSize / 2.0);
                    int chairY = (int) (cy + Math.sin(angle) * r - chairSize / 2.0);
                    g2.fillOval(chairX, chairY, chairSize, chairSize);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawOval(chairX, chairY, chairSize, chairSize);
                    g2.setColor(g2.getColor()); // Restore baseColor.darker()
                }
            } else {
                // Rechteckige Tische: Stühle oben/unten/links/rechts platzieren
                if (seats == 2) {
                    // Oben und Unten
                    g2.fillRoundRect(x + width / 2 - chairSize / 2, y - offset, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + width / 2 - chairSize / 2, y + height + offset - chairSize, chairSize, chairSize, 3, 3);
                } else if (seats == 3) {
                    // Zwei oben, einer unten
                    g2.fillRoundRect(x + width / 4 - chairSize / 2, y - offset, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + 3 * width / 4 - chairSize / 2, y - offset, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + width / 2 - chairSize / 2, y + height + offset - chairSize, chairSize, chairSize, 3, 3);
                } else { // 4 oder mehr Sitze
                    // Zwei oben, zwei unten
                    g2.fillRoundRect(x + width / 4 - chairSize / 2, y - offset, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + 3 * width / 4 - chairSize / 2, y - offset, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + width / 4 - chairSize / 2, y + height + offset - chairSize, chairSize, chairSize, 3, 3);
                    g2.fillRoundRect(x + 3 * width / 4 - chairSize / 2, y + height + offset - chairSize, chairSize, chairSize, 3, 3);
                }
            }
        }
    }
}
