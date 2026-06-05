# Restaurant Management System – PowerPoint Präsentation

---

## Folie 1: Titelfolie

**🍽 Restaurant Management System**

Entwicklung einer vollständigen Java-Anwendung zur Verwaltung eines Restaurants

---

**Präsentation**
- Kurs: Informatik / Software Engineering
- Technologie: Java 17 · Swing · SQLite
- Datum: 05.06.2026

---

## Folie 2: Projektübersicht

### Was wurde entwickelt?

✅ Vollständige Desktop-Anwendung für Restaurantbetrieb  
✅ 6 vollständige Verwaltungsmodule  
✅ Persistente SQLite-Datenbank  
✅ Strukturierte 3-Schichten-Architektur  
✅ Umfangreiche Fehlerbehandlung  
✅ Statistiken & Bonus-Features  

**29 Java-Klassen | 6 GUI-Module | 5 Datenbanktabellen**

---

## Folie 3: Technologie-Stack

| Technologie | Zweck |
|---|---|
| **Java 17** | Programmiersprache (LTS) |
| **Java Swing** | Grafische Benutzeroberfläche |
| **SQLite** | Relationale Datenbank (kein Server nötig) |
| **JDBC** | Datenbankverbindung |
| **Maven** | Build-Management |

**Warum SQLite?**
→ Keine Installation erforderlich  
→ Datenbank = einzelne `.db`-Datei  
→ Ideal für Desktop-Anwendungen  

---

## Folie 4: Architektur – MVC-Pattern

```
┌──────────────────────────────┐
│   VIEW: Java Swing GUI       │
│   6 Panels im Hauptfenster   │
└──────────┬───────────────────┘
           ↓ Aufruf
┌──────────────────────────────┐
│   CONTROLLER: Services       │
│   Validierung & Geschäfts-   │
│   logik                      │
└──────────┬───────────────────┘
           ↓ SQL
┌──────────────────────────────┐
│   MODEL: DAOs + Models       │
│   Datenbankoperationen       │
└──────────┬───────────────────┘
           ↓ JDBC
┌──────────────────────────────┐
│   SQLite Datenbank           │
│   restaurant.db              │
└──────────────────────────────┘
```

**Vorteile des MVC-Musters:**
- Klare Trennung der Verantwortlichkeiten
- Einfach erweiterbar
- Gut testbar

---

## Folie 5: Modul 1 – Speisekarte

### Funktionen:
- 📋 Alle Gerichte in Tabelle anzeigen
- ➕ Neues Gericht hinzufügen
- ✏️ Gericht bearbeiten
- 🗑 Gericht löschen
- ✓/✗ Verfügbarkeit ein-/ausschalten

### Kategorien:
`Vorspeise` | `Hauptgericht` | `Dessert` | `Getränk`

### Daten:
Bereits vorbelegt mit **18 Beispielgerichten**

---

## Folie 6: Modul 2 – Bestellungen

### Ablauf einer Bestellung:
1. **Tisch** auswählen (1–20)
2. **Kunde** optional zuordnen
3. **Gerichte** aus Speisekarte wählen + Menge
4. Position hinzufügen → Gesamtpreis berechnet sich automatisch
5. **"Bestellung anlegen"** → gespeichert

### Status-Lifecycle:
```
OFFEN → IN_BEARBEITUNG → FERTIG → [Rechnung erstellen]
         ↓
      STORNIERT
```

---

## Folie 7: Modul 3 & 4 – Kunden & Reservierungen

### Kundenverwaltung:
- Vollständige CRUD-Operationen
- **Suchfunktion** (Name, Telefon, E-Mail)
- Laufkundschaft ohne Kunden-Zuordnung möglich

### Reservierungen:
- Datum, Uhrzeit, Tisch, Personenanzahl
- Validierung: kein Datum in der Vergangenheit
- Filter: "Heutige Reservierungen" anzeigen
- Stornierung mit Bestätigungsdialog

---

## Folie 8: Modul 5 – Rechnungen

### Rechnungserstellung:
1. Abgeschlossene Bestellung (Status = FERTIG) auswählen
2. "Rechnung erstellen" → automatische Berechnung
3. **MwSt.-Berechnung (19% inklusiv):**
   - Formel: MwSt. = Brutto × 0,19 ÷ 1,19
4. Als "bezahlt" markieren

### Beispiel:
| Position | Betrag |
|---|---|
| Gesamtbetrag (Brutto) | 45,00 € |
| davon MwSt. (19%) | 7,17 € |
| Nettobetrag | 37,83 € |

---

## Folie 9: Modul 6 – Statistiken (BONUS)

### KPI-Karten:
- 📈 **Tagesumsatz** (heute)
- 💰 **Gesamtumsatz** (alle Zeit)
- 📋 **Bestellungen heute**
- 🔢 **Bestellungen gesamt**

### Tabellen:
- Umsatz der letzten **7 Tage**
- **Top 5** beliebteste Gerichte
- Bestellanzahl pro Tag (letzte 7 Tage)
- Umsatz für **beliebiges Datum** abfragen

---

## Folie 10: Fehlerbehandlung

### Exception-Hierarchie:
```
Exception
├── DatabaseException (RuntimeException)
│   → Kapselt SQL-Fehler
│   → z.B.: "Datenbankverbindung fehlgeschlagen"
└── ValidationException
    → Prüft Benutzereingaben
    → z.B.: "Name darf nicht leer sein"
```

### Prinzipien:
- **Alle SQL-Operationen** in try-catch
- **Alle Formulareingaben** validiert vor DB-Zugriff
- **Fehlermeldungen** für Benutzer in verständlichem Deutsch
- **Startfehler** führen zu sauberem Beenden

---

## Folie 11: Datenbankschema

**5 Tabellen mit Foreign-Key-Beziehungen:**

```
customers ←── orders ←── order_items ──→ menu_items
              ↓
customers ←── reservations
              ↓
           invoices ←── orders
```

**Design-Entscheidungen:**
- `unit_price` in `order_items` gespeichert → Preisänderungen beeinflussen alte Bestellungen nicht
- `customer_id` nullable → Laufkundschaft möglich
- `available` in `menu_items` → Gerichte deaktivierbar statt löschen

---

## Folie 12: Entwurfsmuster

| Pattern | Klasse | Vorteil |
|---|---|---|
| **Singleton** | `DatabaseConnection` | Nur 1 DB-Verbindung |
| **DAO Pattern** | Alle `*DAO` Klassen | DB-Logik isoliert |
| **MVC** | Gesamt-Architektur | Klare Trennung |
| **Interface** | `Refreshable` | Einheitliche Tab-Aktualisierung |

---

## Folie 13: Projektstruktur

```
de.restaurant/
├── Main.java                   Einstiegspunkt
├── exception/                  Eigene Fehlerklassen
│   ├── DatabaseException
│   └── ValidationException
├── model/                      Datenklassen (POJOs)
│   ├── Customer, MenuItem
│   ├── Order, OrderItem
│   ├── Reservation, Invoice
├── dao/                        SQL-Schicht
│   └── *DAO.java (6 Klassen)
├── service/                    Geschäftslogik
│   └── *Service.java (6 Klassen)
└── gui/                        Swing-Oberfläche
    ├── MainFrame.java
    └── panels/ (7 Klassen)
```

---

## Folie 14: Start & Bedienung

### Ausführen (JAR):
```bash
java -jar RestaurantSystem-1.0.0-jar-with-dependencies.jar
```

### In IntelliJ IDEA:
1. Projekt öffnen
2. Maven-Import bestätigen
3. `Main.java` → Run

### Datenbankdatei:
- Wird automatisch erstellt: `restaurant.db`
- SQLite Browser zum manuellen Inspizieren nutzbar

### Beim ersten Start:
- Schema wird erstellt
- 5 Kunden + 18 Gerichte werden als Beispieldaten geladen

---

## Folie 15: Fazit & Ausblick

### Realisierte Anforderungen:
✅ Java-Anwendung mit Swing-GUI  
✅ Strukturierte Klassenarchitektur (MVC)  
✅ SQLite-Datenbankanbindung  
✅ Vollständige Fehlerbehandlung  
✅ Sinnvolle Benutzerführung  
✅ Ausführliche Kommentierung  
✅ Tagesumsatz & Statistiken (Bonus)  
✅ UML-Klassendiagramm  
✅ Pseudocode  

### Mögliche Erweiterungen:
- Druckfunktion für Rechnungen (PDF)
- Login/Rollen (Kellner, Manager)
- Mehrsprachigkeit (i18n)
- Cloud-Datenbank (MySQL)

---

*Ende der Präsentation – Fragen?*
