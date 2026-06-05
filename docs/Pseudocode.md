# Pseudocode – Restaurant Management System

## 1. Programmstart

```
PROGRAMM Restaurant Management System STARTEN:

    1. Look & Feel auf System-Standard setzen
    2. Swing Event-Dispatch-Thread starten:
        a. Datenbankverbindung herstellen (Singleton)
            → Schema-Tabellen erstellen (falls nicht vorhanden)
            → Beispieldaten einfügen (falls noch keine Daten)
        b. Hauptfenster (MainFrame) erstellen
        c. Alle Tab-Panels initialisieren
        d. Fenster anzeigen
    3. WENN Fehler DANN Fehlerdialog anzeigen und Programm beenden
```

---

## 2. Speisekarte verwalten (MenuService)

### 2.1 Gericht hinzufügen

```
FUNKTION addMenuItem(name, kategorie, preis, beschreibung, verfügbar):

    WENN name = LEER DANN
        FEHLER: "Name des Gerichts darf nicht leer sein"
        RETURN
    WENN kategorie = LEER DANN
        FEHLER: "Bitte eine Kategorie auswählen"
        RETURN
    WENN preis <= 0 DANN
        FEHLER: "Preis muss größer als 0,00 € sein"
        RETURN

    neues_gericht = erstelle MenuItem(name, kategorie, preis, beschreibung, verfügbar)
    gespeichert = menuItemDAO.insert(neues_gericht)
    RETURN gespeichert
```

### 2.2 Gericht aktualisieren

```
FUNKTION updateMenuItem(gericht):

    Eingaben validieren (wie bei addMenuItem)
    menuItemDAO.update(gericht)
    Tabelle neu laden
```

---

## 3. Bestellung anlegen (OrderService)

```
FUNKTION createOrder(kunde, tischnummer, positionen):

    WENN tischnummer < 1 DANN
        FEHLER: "Tischnummer muss mindestens 1 sein"
        RETURN

    WENN positionen = LEER DANN
        FEHLER: "Mindestens ein Gericht muss bestellt werden"
        RETURN

    neue_bestellung = erstelle Order(kunde, tischnummer, aktuelle_Zeit)
    neue_bestellung.status = "OFFEN"
    neue_bestellung.positionen = positionen

    FUER JEDE position IN positionen:
        position.einzelpreis = gericht.preis  // Preis zum Bestellzeitpunkt festhalten

    gespeicherte_bestellung = orderDAO.insert(neue_bestellung)
    RETURN gespeicherte_bestellung
```

### 3.1 Bestellstatus ändern

```
FUNKTION updateStatus(bestellungsId, neuer_status):

    GÜLTIGE_STATUSWERTE = [OFFEN, IN_BEARBEITUNG, FERTIG, STORNIERT]

    WENN neuer_status NICHT IN GÜLTIGE_STATUSWERTE DANN
        FEHLER: "Ungültiger Status"
        RETURN

    orderDAO.updateStatus(bestellungsId, neuer_status)
```

---

## 4. Reservierung anlegen (ReservationService)

```
FUNKTION createReservation(kunde, tischnummer, datum, uhrzeit, personen, notizen):

    WENN tischnummer < 1 ODER tischnummer > 20 DANN
        FEHLER: "Tischnummer muss zwischen 1 und 20 liegen"
        RETURN

    WENN datum = NULL DANN
        FEHLER: "Bitte ein Datum angeben"
        RETURN

    WENN datum VOR heutigem_Datum DANN
        FEHLER: "Datum darf nicht in der Vergangenheit liegen"
        RETURN

    WENN uhrzeit = NULL DANN
        FEHLER: "Bitte eine Uhrzeit angeben"
        RETURN

    WENN personen < 1 DANN
        FEHLER: "Personenanzahl muss mindestens 1 sein"
        RETURN

    neue_reservierung = erstelle Reservation(kunde, tischnummer, datum, uhrzeit, personen, notizen)
    gespeichert = reservationDAO.insert(neue_reservierung)
    RETURN gespeichert
```

---

## 5. Rechnung erstellen (InvoiceService)

```
FUNKTION createInvoice(bestellungsId):

    bestellung = orderDAO.findById(bestellungsId)

    WENN bestellung = NULL DANN
        FEHLER: "Bestellung nicht gefunden"
        RETURN

    WENN invoiceDAO.existsForOrder(bestellungsId) DANN
        FEHLER: "Für diese Bestellung existiert bereits eine Rechnung"
        RETURN

    WENN bestellung.positionen = LEER DANN
        FEHLER: "Bestellung enthält keine Positionen"
        RETURN

    gesamtbetrag = 0
    FUER JEDE position IN bestellung.positionen:
        gesamtbetrag = gesamtbetrag + (position.menge × position.einzelpreis)

    // MwSt. aus Bruttobetrag herausrechnen (inkl. Steuer)
    // Formel: MwSt. = Brutto × 0,19 ÷ 1,19
    mwst_betrag = gesamtbetrag × 19 ÷ 119

    neue_rechnung = erstelle Invoice:
        → bestellung = bestellung
        → gesamtbetrag = gesamtbetrag
        → mwst = mwst_betrag
        → datum = aktuelle_Zeit
        → bezahlt = FALSCH

    gespeichert = invoiceDAO.insert(neue_rechnung)
    RETURN gespeichert
```

---

## 6. Tagesumsatz berechnen (ReportService)

```
FUNKTION getDailyRevenue(datum):

    SQL_ABFRAGE = "
        SELECT SUMME(gesamtbetrag)
        FROM rechnungen
        WHERE rechnungsdatum BEGINNT_MIT datum
        UND bezahlt = WAHR
    "

    ergebnis = datenbank.ausführen(SQL_ABFRAGE)

    WENN ergebnis = NULL DANN
        RETURN 0.00

    RETURN ergebnis als double
```

---

## 7. Datenbankzugriff (DAO-Pattern)

### 7.1 Kunden-Insert

```
FUNKTION CustomerDAO.insert(kunde):

    SQL = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)"

    VERSUCHE:
        statement = datenbankverbindung.vorbereiten(SQL)
        statement.setze(1, kunde.name)
        statement.setze(2, kunde.phone)
        statement.setze(3, kunde.email)
        statement.ausführen()
        
        generierte_id = statement.generierterSchlüssel()
        kunde.id = generierte_id
        RETURN kunde

    BEI FEHLER (SQLException):
        WERFE DatabaseException("Fehler beim Einfügen des Kunden: " + fehler.nachricht)
```

---

## 8. GUI-Interaktion (Beispiel: Neuen Kunden anlegen)

```
WENN Benutzer auf "Hinzufügen" klickt:

    name = eingabefeld_name.text
    telefon = eingabefeld_telefon.text
    email = eingabefeld_email.text

    VERSUCHE:
        neuer_kunde = customerService.createCustomer(name, telefon, email)
        kundentabelle.neu_laden()
        formular.leeren()
        dialog.zeige("Kunde erfolgreich angelegt")

    BEI ValidationException:
        dialog.zeige_fehler(exception.nachricht)

    BEI DatabaseException:
        dialog.zeige_fehler("Datenbankfehler: " + exception.nachricht)
```

---

## 9. Datenbankinitialisierung (Singleton)

```
KLASSE DatabaseConnection:

    WENN instance = NULL DANN (nur beim ersten Aufruf):

        1. JDBC-Treiber laden ("org.sqlite.JDBC")
        2. Verbindung öffnen: "jdbc:sqlite:restaurant.db"
        3. Foreign Keys aktivieren: PRAGMA foreign_keys = ON
        4. schema.sql ausführen (Tabellen erstellen falls nicht vorhanden)
        5. sample_data.sql ausführen (Beispieldaten einfügen)

    RETURN instance (immer dieselbe Instanz)
```
