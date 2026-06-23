-- ============================================================
-- Restaurant Management System – Beispieldaten
-- Datei: sample_data.sql
-- Beschreibung: Befüllt die Datenbank mit Testdaten
-- ============================================================

-- Beispielkunden (mit expliziten IDs, um Duplikate bei App-Neustarts zu verhindern)
INSERT OR IGNORE INTO customers (id, name, phone, email) VALUES
    (1, 'Max Mustermann',  '0151-12345678', 'max@example.com'),
    (2, 'Anna Schmidt',    '0160-98765432', 'anna@example.com'),
    (3, 'Peter Müller',    '0170-11223344', 'peter@example.com'),
    (4, 'Lisa Weber',      '0178-55667788', 'lisa@example.com'),
    (5, 'Thomas Braun',    '0152-99887766', 'thomas@example.com');

-- Speisekarte – Vorspeisen (mit expliziten IDs, um Duplikate bei App-Neustarts zu verhindern)
INSERT OR IGNORE INTO menu_items (id, name, category, price, description, available) VALUES
    (1, 'Tomatensuppe',        'Vorspeise', 6.50,  'Hausgemachte Tomatencremesuppe',          1),
    (2, 'Bruschetta',          'Vorspeise', 7.90,  'Geröstetes Brot mit Tomaten und Basilikum',1),
    (3, 'Garnelencocktail',    'Vorspeise', 9.90,  'Garnelen mit Cocktailsauce und Salat',    1),
    (4, 'Caesar Salad',        'Vorspeise', 8.50,  'Römersalat, Croutons, Parmesan',          1);

-- Speisekarte – Hauptgerichte
INSERT OR IGNORE INTO menu_items (id, name, category, price, description, available) VALUES
    (5, 'Wiener Schnitzel',    'Hauptgericht', 18.90, 'Klassisches Kalbsschnitzel mit Pommes', 1),
    (6, 'Rinderfilet',         'Hauptgericht', 28.50, '200g Filet mit Pfeffersauce',           1),
    (7, 'Lachs gegrillt',      'Hauptgericht', 22.00, 'Mit Dillsauce und Reis',                1),
    (8, 'Pasta Carbonara',     'Hauptgericht', 13.50, 'Spaghetti mit Speck und Ei',            1),
    (9, 'Vegane Bowl',         'Hauptgericht', 15.90, 'Quinoa, Avocado, Röstzwiebeln',         1),
    (10, 'Hähnchen Tikka',      'Hauptgericht', 17.50, 'Mit Basmatireis und Naan',              1);

-- Speisekarte – Desserts
INSERT OR IGNORE INTO menu_items (id, name, category, price, description, available) VALUES
    (11, 'Tiramisu',            'Dessert', 7.50, 'Klassisch mit Mascarpone',              1),
    (12, 'Crème Brûlée',        'Dessert', 6.90, 'Vanille-Crème mit Karamellkruste',      1),
    (13, 'Schokoladenkuchen',   'Dessert', 6.50, 'Warm, mit Vanilleeis',                  1);

-- Speisekarte – Getränke
INSERT OR IGNORE INTO menu_items (id, name, category, price, description, available) VALUES
    (14, 'Wasser (0,5l)',        'Getränk', 3.20, 'Still oder Sprudelnd',           1),
    (15, 'Cola (0,33l)',         'Getränk', 3.50, 'Coca-Cola',                      1),
    (16, 'Weißwein (0,2l)',      'Getränk', 5.90, 'Grauburgunder, trocken',         1),
    (17, 'Rotwein (0,2l)',       'Getränk', 6.20, 'Dornfelder, halbtrocken',        1),
    (18, 'Bier vom Fass (0,5l)', 'Getränk', 4.50, 'Pilsner',                        1),
    (19, 'Espresso',             'Getränk', 2.80, 'Doppelter Espresso',             1);

INSERT OR IGNORE INTO ingredients (id, name, quantity, unit, min_stock)
VALUES
  (1, 'Mehl',        50.0, 'kg',  10.0),
  (2, 'Tomaten',     30.0, 'kg',   5.0),
  (3, 'Käse',        15.0, 'kg',   3.0),
  (4, 'Olivenöl',    10.0, 'L',    2.0),
  (5, 'Salz',        20.0, 'kg',   2.0),
  (6, 'Hähnchenb.',  25.0, 'kg',   5.0),
  (7, 'Rindfleisch', 20.0, 'kg',   5.0),
  (8, 'Nudeln',      40.0, 'kg',   8.0);

-- Verknüpfung zwischen Gerichten und Zutaten (Rezepturen)
INSERT OR IGNORE INTO menu_item_ingredients (menu_item_id, ingredient_id, amount_needed) VALUES
    -- Tomatensuppe (ID 1) -> Tomaten 0.3kg, Olivenöl 0.01L, Salz 0.005kg
    (1, 2, 0.3),
    (1, 4, 0.01),
    (1, 5, 0.005),
    
    -- Bruschetta (ID 2) -> Tomaten 0.15kg, Olivenöl 0.02L, Mehl 0.05kg
    (2, 2, 0.15),
    (2, 4, 0.02),
    (2, 1, 0.05),
    
    -- Wiener Schnitzel (ID 5) -> Mehl 0.1kg
    (5, 1, 0.1),
    
    -- Rinderfilet (ID 6) -> Rindfleisch 0.22kg, Salz 0.01kg
    (6, 7, 0.22),
    (6, 5, 0.01),
    
    -- Pasta Carbonara (ID 8) -> Nudeln 0.15kg, Käse 0.05kg, Salz 0.01kg
    (8, 8, 0.15),
    (8, 3, 0.05),
    (8, 5, 0.01),
    
    -- Hähnchen Tikka (ID 10) -> Hähnchenbrust 0.25kg, Salz 0.01kg
    (10, 6, 0.25),
    (10, 5, 0.01);

-- Beispiellogin-Daten für Benutzer
INSERT OR IGNORE INTO users (id, username, password, role) VALUES
    (1, 'admin',   'admin123',   'ADMIN'),
    (2, 'manager', 'manager123', 'MANAGER'),
    (3, 'staff',   'staff123',   'STAFF');
