# Nutrition Tracker

Ein moderner, intelligenter und zu **100 % lokaler** Ernährungs- und Gesundheits-Tracker für Android. Entwickelt mit **Kotlin** und **Jetpack Compose**.
Diese App gibt dir die volle Kontrolle über deine Daten.

---

## Features

### Tagebuch & Tracking
* **Barcode-Scanner:** Erfasse Lebensmittel über die Kamera.
* **Suche:** Filtere deine zuletzt verwendeten Produkte und Mahlzeiten.
* **Anpassung:** Ändere die Grammzahl von Einträgen direkt im Tagebuch.
* **Eigene Lebensmittel:** Lege individuelle Produkte mit exakten Makronährstoffen an.

### Mahlzeiten
* **Mahlzeiten:** Kombiniere Produkte zu einer festen Mahlzeiten und speichere sie fürs Tracking.
* **Share & Export:** Exportiere einzelne Lebensmittel oder ganze Rezepte als `.json`-Datei und teile sie mit Freunden.

### Analyse & Ziele
* **Kalorienrechner:** Berechnet TDEE, BMI und Makro-Verteilungen.
* **Graphen:** Gewichtsverläufe, Trendlinien und Projektions-Szenarien (z.B. theoretischer Verlauf bei -500 kcal).
* **Makro-Splits:** Kreisdiagramme und Tagesbilanzen.
* **Kalender:** Übersichtlicher Wochenkalender mit Indikator-Punkten für aktive Tracking-Tage.

### Health Connect Integration
* **Automatische Synchronisation:** Zieht Schritte und verbrannte Aktiv-Kalorien direkt aus Google Fit / Health Connect.
* **Fallback:** Sendet Health connect keine Kaloriendaten? Die App errechnet den Verbrauch automatisch anhand deiner Schrittzahl.

### Optionen & Automatisierung
* **System-Erinnerungen:** Android-Hintergrund-Benachrichtigungen für Wasser-Ziele, offene Kalorien oder individuelle Supplements mit eigenen Texten.
* **Auto-Backup-System:** Erstellt täglich oder wöchentlich ZIP-Backups der gesamten Datenbank in einem lokalen Ordner deiner Wahl.
* **Dark Mode:** Vollständige Unterstützung für das dunkle System-Theme.

---

## Tech Stack

* **Sprache:** Kotlin
* **UI-Toolkit:** Jetpack Compose (Material Design 3)
* **Architektur:** MVVM (Model-View-ViewModel) mit Kotlin Coroutines & Flows
* **Lokale Speicherung:** Room Database (SQLite), SharedPreferences
* **Schnittstellen:** Health Connect API, CameraX (Barcode Scanning)

---

