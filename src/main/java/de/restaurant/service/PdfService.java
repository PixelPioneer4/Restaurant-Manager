package de.restaurant.service;

import de.restaurant.model.Invoice;
import de.restaurant.model.OrderItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;

/**
 * Service zum Generieren von Rechnungs-PDFs mit Apache PDFBox.
 */
public class PdfService {

    /**
     * Generiert eine PDF-Datei für die übergebene Rechnung.
     * Speicherort: {user.home}/RestaurantSystem/invoices/Rechnung_{id}.pdf
     * @param invoice Die Rechnung, die exportiert werden soll
     * @return Das generierte File-Objekt
     * @throws IOException falls ein Fehler beim Schreiben der Datei auftritt
     */
    public File generateInvoicePdf(Invoice invoice) throws IOException {
        // Zielverzeichnis erstellen
        File dir = new File(System.getProperty("user.home"), "RestaurantSystem/invoices");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File pdfFile = new File(dir, "Rechnung_" + invoice.getId() + ".pdf");

        try (PDDocument document = new PDDocument()) {
            // A4 Seite hinzufügen
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Fonts instanziieren (in PDFBox 3.x sind dies keine Singletons mehr)
            PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Header: "Restaurant Management System"
                drawText(cs, "Restaurant Management System", boldFont, 20, 50, 780);

                // Trennlinie unter Header
                cs.moveTo(50, 765);
                cs.lineTo(545, 765);
                cs.stroke();

                // Rechnungsdetails (Metadaten)
                String dateStr = invoice.getIssueDate().toString().replace("T", " ").substring(0, 16);
                drawText(cs, "Rechnungsnummer: #" + invoice.getId(), regularFont, 11, 50, 740);
                drawText(cs, "Datum:           " + dateStr, regularFont, 11, 50, 720);
                drawText(cs, "Zahlungsmethode: " + invoice.getPaymentMethod(), regularFont, 11, 50, 700);
                drawText(cs, "Tisch:           Tisch " + invoice.getOrder().getTableNumber(), regularFont, 11, 50, 680);

                // Tabellen-Header
                drawText(cs, "Pos", boldFont, 11, 50, 640);
                drawText(cs, "Gericht", boldFont, 11, 90, 640);
                drawText(cs, "Menge", boldFont, 11, 300, 640);
                drawText(cs, "Einzelpreis", boldFont, 11, 370, 640);
                drawText(cs, "Gesamt", boldFont, 11, 470, 640);

                // Trennlinie unter Header
                cs.moveTo(50, 630);
                cs.lineTo(545, 630);
                cs.stroke();

                // Artikel-Tabellenzeilen zeichnen
                float currentY = 610;
                int pos = 1;
                for (OrderItem item : invoice.getOrder().getItems()) {
                    drawText(cs, String.valueOf(pos), regularFont, 10, 50, currentY);
                    drawText(cs, cleanText(item.getMenuItem().getName()), regularFont, 10, 90, currentY);
                    drawText(cs, String.valueOf(item.getQuantity()), regularFont, 10, 300, currentY);
                    drawText(cs, String.format("%.2f EUR", item.getUnitPrice()), regularFont, 10, 370, currentY);
                    drawText(cs, String.format("%.2f EUR", item.getSubtotal()), regularFont, 10, 470, currentY);

                    currentY -= 20;
                    pos++;
                }

                // Trennlinie unter Artikeltabelle
                cs.moveTo(50, currentY + 5);
                cs.lineTo(545, currentY + 5);
                cs.stroke();

                // Summenbereich berechnen und zeichnen
                currentY -= 15;
                drawText(cs, "Zwischensumme:", regularFont, 11, 350, currentY);
                drawText(cs, String.format("%.2f EUR", invoice.getNetAmount()), regularFont, 11, 470, currentY);

                currentY -= 20;
                drawText(cs, "MwSt. (19%):", regularFont, 11, 350, currentY);
                drawText(cs, String.format("%.2f EUR", invoice.getTaxAmount()), regularFont, 11, 470, currentY);

                currentY -= 20;
                drawText(cs, "Gesamtbetrag:", boldFont, 11, 350, currentY);
                drawText(cs, String.format("%.2f EUR", invoice.getTotalAmount()), boldFont, 11, 470, currentY);

                // Footer-Trennlinie
                cs.moveTo(50, 100);
                cs.lineTo(545, 100);
                cs.stroke();

                // Footer-Text
                drawText(cs, "Vielen Dank fuer Ihren Besuch!", boldFont, 11, 190, 80);
            }

            // PDF-Dokument abspeichern
            document.save(pdfFile);
        }

        return pdfFile;
    }

    /**
     * Hilfsmethode zum Zeichnen von Text in einem ContentStream.
     */
    private void drawText(PDPageContentStream cs, String text, PDType1Font font, float fontSize, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Hilfsmethode zum Normalisieren von Texten für PDFBox Helvetica Fonts.
     * Ersetzt deutsche Umlaute und Sonderzeichen durch ihre ASCII-Entsprechungen.
     */
    private String cleanText(String text) {
        if (text == null) return "";
        return text.replace("ä", "ae")
                   .replace("ö", "oe")
                   .replace("ü", "ue")
                   .replace("Ä", "Ae")
                   .replace("Ö", "Oe")
                   .replace("Ü", "Ue")
                   .replace("ß", "ss")
                   .replace("é", "e")
                   .replace("è", "e")
                   .replace("ê", "e")
                   .replace("û", "u")
                   .replace("â", "a");
    }
}
