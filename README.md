# Restaurant Management System

Ein vollständiges Restaurant-Verwaltungssystem in Java mit Swing-GUI und SQLite-Datenbank.

## Features

- 📋 **Speisekarte verwalten** – Gerichte hinzufügen, bearbeiten, löschen
- 🛒 **Bestellungen anlegen** – Tisch + Gericht + Menge
- 👤 **Kundenverwaltung** – CRUD + Suchfunktion
- 📅 **Reservierungen** – Mit Datum-/Zeitvalidierung
- 🧾 **Rechnungsübersicht** – Automatische MwSt.-Berechnung (19%)
- 📊 **Statistiken** – Tagesumsatz, Top-Gerichte, Bestellstatistiken

## Voraussetzungen

- Java 17+
- Maven 3.6+

## Bauen & Starten

```bash
# Bauen
mvn clean package

# Starten
java -jar target/RestaurantSystem-1.0.0-jar-with-dependencies.jar
```

## Projektstruktur

```
src/main/java/de/restaurant/
├── Main.java                    Einstiegspunkt
├── exception/                   Eigene Exceptions
├── model/                       Datenklassen
├── dao/                         Datenbankzugriff
├── service/                     Geschäftslogik
└── gui/panels/                  Swing-GUI-Module
```

## Architektur

Das Projekt folgt dem **MVC-Pattern** mit klarer Schichtentrennung:

`GUI → Service (Validierung) → DAO (SQL) → SQLite`

## Technologien

| Technologie | Version |
|---|---|
| Java | 17 |
| Java Swing | JDK-integriert |
| SQLite JDBC | 3.45.3.0 |
| Maven | 3.6+ |

## Datenbank

Die SQLite-Datenbank `restaurant.db` wird automatisch beim ersten Start erstellt.  
Beim ersten Start werden Beispieldaten geladen (5 Kunden, 18 Gerichte).

## Dokumentation

Alle Dokumente befinden sich im Ordner `docs/`:

- `Projektdokumentation.md` – Vollständige technische Dokumentation
- `UML_Klassendiagramm.md` – Mermaid UML-Diagramm
- `Pseudocode.md` – Pseudocode der Kernfunktionen
- `Praesentation.md` – PowerPoint-Präsentationsinhalt
