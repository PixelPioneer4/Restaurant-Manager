-- ============================================================
-- Restaurant Management System – Beispieldaten
-- Datei: sample_data.sql
-- Beschreibung: Befüllt die Datenbank mit Testdaten
-- ============================================================

-- Beispielkunden
INSERT OR IGNORE INTO customers (name, phone, email) VALUES
    ('Max Mustermann',  '0151-12345678', 'max@example.com'),
    ('Anna Schmidt',    '0160-98765432', 'anna@example.com'),
    ('Peter Müller',    '0170-11223344', 'peter@example.com'),
    ('Lisa Weber',      '0178-55667788', 'lisa@example.com'),
    ('Thomas Braun',    '0152-99887766', 'thomas@example.com');

-- Speisekarte – Vorspeisen
INSERT OR IGNORE INTO menu_items (name, category, price, description, available) VALUES
    ('Tomatensuppe',        'Vorspeise', 6.50,  'Hausgemachte Tomatencremesuppe',          1),
    ('Bruschetta',          'Vorspeise', 7.90,  'Geröstetes Brot mit Tomaten und Basilikum',1),
    ('Garnelencocktail',    'Vorspeise', 9.90,  'Garnelen mit Cocktailsauce und Salat',    1),
    ('Caesar Salad',        'Vorspeise', 8.50,  'Römersalat, Croutons, Parmesan',          1);

-- Speisekarte – Hauptgerichte
INSERT OR IGNORE INTO menu_items (name, category, price, description, available) VALUES
    ('Wiener Schnitzel',    'Hauptgericht', 18.90, 'Klassisches Kalbsschnitzel mit Pommes', 1),
    ('Rinderfilet',         'Hauptgericht', 28.50, '200g Filet mit Pfeffersauce',           1),
    ('Lachs gegrillt',      'Hauptgericht', 22.00, 'Mit Dillsauce und Reis',                1),
    ('Pasta Carbonara',     'Hauptgericht', 13.50, 'Spaghetti mit Speck und Ei',            1),
    ('Vegane Bowl',         'Hauptgericht', 15.90, 'Quinoa, Avocado, Röstzwiebeln',         1),
    ('Hähnchen Tikka',      'Hauptgericht', 17.50, 'Mit Basmatireis und Naan',              1);

-- Speisekarte – Desserts
INSERT OR IGNORE INTO menu_items (name, category, price, description, available) VALUES
    ('Tiramisu',            'Dessert', 7.50, 'Klassisch mit Mascarpone',              1),
    ('Crème Brûlée',        'Dessert', 6.90, 'Vanille-Crème mit Karamellkruste',      1),
    ('Schokoladenkuchen',   'Dessert', 6.50, 'Warm, mit Vanilleeis',                  1);

-- Speisekarte – Getränke
INSERT OR IGNORE INTO menu_items (name, category, price, description, available) VALUES
    ('Wasser (0,5l)',        'Getränk', 3.20, 'Still oder Sprudelnd',           1),
    ('Cola (0,33l)',         'Getränk', 3.50, 'Coca-Cola',                      1),
    ('Weißwein (0,2l)',      'Getränk', 5.90, 'Grauburgunder, trocken',         1),
    ('Rotwein (0,2l)',       'Getränk', 6.20, 'Dornfelder, halbtrocken',        1),
    ('Bier vom Fass (0,5l)', 'Getränk', 4.50, 'Pilsner',                        1),
    ('Espresso',             'Getränk', 2.80, 'Doppelter Espresso',             1);
