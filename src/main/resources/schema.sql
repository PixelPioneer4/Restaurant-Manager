-- ============================================================
-- Restaurant Management System – Datenbankschema
-- Datei: schema.sql
-- Beschreibung: Erstellt alle benötigten Tabellen beim Start
-- ============================================================

-- Kunden-Tabelle
CREATE TABLE IF NOT EXISTS customers (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT    NOT NULL,
    phone   TEXT,
    email   TEXT
);

-- Speisekarten-Tabelle
CREATE TABLE IF NOT EXISTS menu_items (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    category    TEXT    NOT NULL,   -- Vorspeise, Hauptgericht, Dessert, Getränk
    price       REAL    NOT NULL,
    description TEXT,
    available   INTEGER DEFAULT 1   -- 1 = verfügbar, 0 = nicht verfügbar
);

-- Bestellungen (Kopfdaten)
CREATE TABLE IF NOT EXISTS orders (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id  INTEGER,
    table_number INTEGER NOT NULL,
    order_date   TEXT    NOT NULL,  -- ISO-Format: YYYY-MM-DD HH:MM:SS
    status       TEXT    DEFAULT 'OFFEN',  -- OFFEN, IN_BEARBEITUNG, FERTIG, STORNIERT
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Bestellpositionen
CREATE TABLE IF NOT EXISTS order_items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id     INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,
    quantity     INTEGER NOT NULL,
    unit_price   REAL    NOT NULL,  -- Preis zum Zeitpunkt der Bestellung
    FOREIGN KEY (order_id)     REFERENCES orders(id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Reservierungen
CREATE TABLE IF NOT EXISTS reservations (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id      INTEGER,
    table_number     INTEGER NOT NULL,
    reservation_date TEXT    NOT NULL,  -- YYYY-MM-DD
    reservation_time TEXT    NOT NULL,  -- HH:MM
    guest_count      INTEGER,
    notes            TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Rechnungen
CREATE TABLE IF NOT EXISTS invoices (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id     INTEGER NOT NULL UNIQUE,
    total_amount REAL    NOT NULL,
    tax_amount   REAL    NOT NULL,  -- 19% MwSt.
    issue_date   TEXT    NOT NULL,
    paid         INTEGER DEFAULT 0, -- 0 = offen, 1 = bezahlt
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
