# Projektdokumentation – Restaurant Management System

**Kurs:** Informatik / Software Engineering  
**Version:** 1.0  
**Datum:** 05.06.2026  
**Technologie:** Java 17, Swing GUI, SQLite

---

## 1. Projektbeschreibung

Das **Restaurant Management System** ist eine vollständige Desktop-Anwendung zur Verwaltung eines Restaurants. Sie wurde in Java mit einer grafischen Benutzeroberfläche (Java Swing) entwickelt und nutzt eine SQLite-Datenbank zur persistenten Datenspeicherung.

### 1.1 Ziel des Projekts

Entwicklung eines vollständigen, praxisnahen Verwaltungssystems für ein Restaurant, das folgende Kernaufgaben abdeckt:

- Verwaltung der Speisekarte (CRUD-Operationen)
- Anlegen und Verwalten von Bestellungen
- Kundenverwaltung mit Suchfunktion
- Tischreservierungen
- Rechnungsstellung mit MwSt.-Berechnung
- Statistiken und Tagesumsatz (Bonus)

---

## 2. Technischer Stack

| Technologie | Version | Verwendungszweck |
|---|---|---|
| Java | 17 (LTS) | Programmiersprache |
| Java Swing | JDK-integriert | Grafische Benutzeroberfläche |
| SQLite | via JDBC 3.45 | Relationale Datenbank |
| Maven | 3.9+ | Build-Management |
| SQLite JDBC | 3.45.3.0 | Datenbankverbindung |

---

## 3. Architektur

Das System folgt dem **MVC-Entwurfsmuster** (Model-View-Controller):

```
┌─────────────────────────────────────────┐
│                    GUI                   │
│  MainFrame + Panels (MenuPanel,          │
│  OrderPanel, CustomerPanel, ...)         │
└──────────────┬──────────────────────────┘
               │ ruft auf
┌──────────────▼──────────────────────────┐
│              SERVICE-SCHICHT            │
│  CustomerService, MenuService,          │
│  OrderService, InvoiceService,          │
│  ReservationService, ReportService      │
└──────────────┬──────────────────────────┘
               │ ruft auf
┌──────────────▼──────────────────────────┐
│              DAO-SCHICHT                │
│  CustomerDAO, MenuItemDAO, OrderDAO,    │
│  ReservationDAO, InvoiceDAO             │
└──────────────┬──────────────────────────┘
               │ JDBC
┌──────────────▼──────────────────────────┐
│              SQLite Datenbank            │
│              restaurant.db              │
└─────────────────────────────────────────┘
```

### 3.1 Schichtenarchitektur

**GUI-Schicht (View):** Alle Java Swing Panels und Dialoge. Nimmt Benutzereingaben entgegen und zeigt Daten an.

**Service-Schicht (Controller):** Enthält die Geschäftslogik. Validiert Eingaben und orchestriert DAO-Aufrufe.

**DAO-Schicht (Model):** Data Access Objects kapseln alle SQL-Operationen. Trennt DB-Logik von Geschäftslogik.

**Model-Klassen:** POJOs (Plain Old Java Objects) repräsentieren die Entitäten (Customer, MenuItem, Order, etc.).

---

## 4. Klassenstruktur

### 4.1 Model-Klassen

| Klasse | Beschreibung | Wichtige Felder |
|---|---|---|
| `Customer` | Kundendaten | id, name, phone, email |
| `MenuItem` | Speisekarten-Eintrag | id, name, category, price, available |
| `Order` | Bestellung (Kopf) | id, customer, tableNumber, orderDate, status |
| `OrderItem` | Bestellposition | id, orderId, menuItem, quantity, unitPrice |
| `Reservation` | Tischreservierung | id, customer, tableNumber, date, time, guestCount |
| `Invoice` | Rechnung | id, order, totalAmount, taxAmount, paid |

### 4.2 DAO-Klassen (Data Access Objects)

| Klasse | Tabelle | Operationen |
|---|---|---|
| `DatabaseConnection` | – | Singleton, Verbindung, Schema-Init |
| `CustomerDAO` | customers | insert, update, delete, findAll, search, findById |
| `MenuItemDAO` | menu_items | insert, update, delete, findAll, findByCategory, findAvailable |
| `OrderDAO` | orders, order_items | insert, updateStatus, findAll, findById, findByDate |
| `ReservationDAO` | reservations | insert, delete, findAll, findByDate |
| `InvoiceDAO` | invoices | insert, markAsPaid, existsForOrder, findAll, getDailyRevenue |

### 4.3 Service-Klassen

| Klasse | Aufgabe |
|---|---|
| `CustomerService` | Validierung + Kundenverwaltung |
| `MenuService` | Validierung + Speisekartenverwaltung |
| `OrderService` | Bestelllebenszyklus-Management |
| `ReservationService` | Reservierungs-Validierung + Verwaltung |
| `InvoiceService` | Rechnungserstellung, MwSt.-Berechnung |
| `ReportService` | Statistiken, Tagesumsatz, Top-Gerichte |

---

## 5. Datenbankschema

### Entity-Relationship-Diagramm (ERD)

```
customers           reservations
┌──────────┐        ┌─────────────────┐
│ id (PK)  │◄───────│ customer_id (FK)│
│ name     │        │ table_number    │
│ phone    │        │ reservation_date│
│ email    │        │ reservation_time│
└──────────┘        │ guest_count     │
     │              │ notes           │
     │              └─────────────────┘
     │
     │              orders
     └──────────────┌──────────────┐
                    │ id (PK)      │
                    │ customer_id  │
                    │ table_number │
                    │ order_date   │
                    │ status       │
                    └──────┬───────┘
                           │ 1:N
                    order_items
                    ┌──────────────┐
                    │ id (PK)      │
                    │ order_id(FK) │◄──── invoices
                    │ menu_item_id │      ┌────────────┐
                    │ quantity     │      │ id (PK)    │
                    │ unit_price   │      │ order_id   │
                    └──────────────┘      │ total_amt  │
                                         │ tax_amount │
menu_items                               │ paid       │
┌──────────────┐                         └────────────┘
│ id (PK)      │
│ name         │
│ category     │
│ price        │
│ description  │
│ available    │
└──────────────┘
```

### Tabellen und Datentypen

**customers**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `name` TEXT NOT NULL
- `phone` TEXT
- `email` TEXT

**menu_items**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `name` TEXT NOT NULL
- `category` TEXT NOT NULL (Vorspeise, Hauptgericht, Dessert, Getränk)
- `price` REAL NOT NULL
- `description` TEXT
- `available` INTEGER DEFAULT 1

**orders**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `customer_id` INTEGER (FK → customers, nullable)
- `table_number` INTEGER NOT NULL
- `order_date` TEXT (ISO-Format: YYYY-MM-DD HH:MM:SS)
- `status` TEXT DEFAULT 'OFFEN'

**order_items**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `order_id` INTEGER NOT NULL (FK → orders)
- `menu_item_id` INTEGER NOT NULL (FK → menu_items)
- `quantity` INTEGER NOT NULL
- `unit_price` REAL NOT NULL

**reservations**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `customer_id` INTEGER (FK → customers)
- `table_number` INTEGER NOT NULL
- `reservation_date` TEXT (YYYY-MM-DD)
- `reservation_time` TEXT (HH:MM)
- `guest_count` INTEGER
- `notes` TEXT

**invoices**
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `order_id` INTEGER NOT NULL UNIQUE (FK → orders)
- `total_amount` REAL NOT NULL
- `tax_amount` REAL NOT NULL
- `issue_date` TEXT (YYYY-MM-DD HH:MM:SS)
- `paid` INTEGER DEFAULT 0

---

## 6. GUI-Module

### 6.1 Hauptfenster (MainFrame)
Das Hauptfenster verwendet ein `JTabbedPane` mit 6 Tabs:

| Tab | Panel | Funktion |
|---|---|---|
| 📋 Speisekarte | MenuPanel | Gerichte verwalten |
| 🛒 Bestellungen | OrderPanel | Bestellungen anlegen |
| 👤 Kunden | CustomerPanel | Kundenverwaltung |
| 📅 Reservierungen | ReservationPanel | Tischreservierungen |
| 🧾 Rechnungen | InvoicePanel | Rechnungsübersicht |
| 📊 Statistiken | StatisticsPanel | Umsatz & Berichte |

### 6.2 Benutzerführung

Jedes Panel folgt dem gleichen Grundprinzip:
1. **Tabelle** zeigt alle vorhandenen Datensätze
2. **Formular** rechts zum Eingeben/Bearbeiten
3. **Buttons** für die Aktionen (Hinzufügen, Bearbeiten, Löschen)
4. **Fehlermeldungen** erscheinen als Dialog mit klarer Beschreibung

---

## 7. Fehlerbehandlung

### 7.1 Exception-Hierarchie

```
Exception
├── RuntimeException
│   └── DatabaseException   (eigene Klasse)
│       → Kapselt SQL-Exceptions in anwendungsspezifische Fehler
└── Exception
    └── ValidationException  (eigene Klasse)
        → Validierungsfehler bei Benutzereingaben
```

### 7.2 Fehlerbehandlungsprinzipien

1. **SQL-Fehler** werden in `DatabaseException` gekapselt und mit verständlicher Nachricht weitergegeben
2. **Validierungsfehler** (leere Pflichtfelder, ungültige Preise) werden in der Service-Schicht geprüft und als `ValidationException` geworfen
3. **GUI-Fehler** werden als `JOptionPane`-Dialog mit klarer Fehlermeldung angezeigt
4. **Startfehler** führen zu einem Fehlerdialog und sauberem Beenden der Anwendung

---

## 8. Entwurfsmuster (Design Patterns)

| Pattern | Einsatz |
|---|---|
| **Singleton** | `DatabaseConnection` – nur eine DB-Verbindung |
| **DAO Pattern** | Trennung von Datenbanklogik und Geschäftslogik |
| **MVC** | Trennung von Model, View (Panels) und Controller (Services) |
| **Template Method** | Alle Panels implementieren `Refreshable.refresh()` |

---

## 9. Build & Start

### 9.1 Voraussetzungen
- Java 17 oder höher
- Maven 3.6+

### 9.2 Bauen
```bash
mvn clean package
```

### 9.3 Starten
```bash
java -jar target/RestaurantSystem-1.0.0-jar-with-dependencies.jar
```

### 9.4 Mit IntelliJ IDEA
1. Projekt öffnen: `File > Open > RestaurantSystem`
2. Maven importiert automatisch die Abhängigkeiten
3. `Main.java` mit ▶ ausführen

---

## 10. Verzeichnisstruktur

```
RestaurantSystem/
├── pom.xml                              Maven Build
├── src/main/java/de/restaurant/
│   ├── Main.java                        Einstiegspunkt
│   ├── exception/
│   │   ├── DatabaseException.java       DB-Fehler
│   │   └── ValidationException.java    Eingabe-Fehler
│   ├── model/
│   │   ├── Customer.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Reservation.java
│   │   └── Invoice.java
│   ├── dao/
│   │   ├── DatabaseConnection.java     Singleton
│   │   ├── CustomerDAO.java
│   │   ├── MenuItemDAO.java
│   │   ├── OrderDAO.java
│   │   ├── ReservationDAO.java
│   │   └── InvoiceDAO.java
│   ├── service/
│   │   ├── CustomerService.java
│   │   ├── MenuService.java
│   │   ├── OrderService.java
│   │   ├── ReservationService.java
│   │   ├── InvoiceService.java
│   │   └── ReportService.java
│   └── gui/
│       ├── MainFrame.java
│       └── panels/
│           ├── Refreshable.java
│           ├── MenuPanel.java
│           ├── OrderPanel.java
│           ├── CustomerPanel.java
│           ├── ReservationPanel.java
│           ├── InvoicePanel.java
│           └── StatisticsPanel.java
└── src/main/resources/
    ├── schema.sql                       DB-Schema
    └── sample_data.sql                  Testdaten
```
