# OPP TIMER 6.1 — Warszawa i okolice

Projekt Android (wersja testowa). Mapa OpenStreetMap / osmdroid.

## Funkcje
- GPS i prędkość z usługi pierwszoplanowej
- Mapa automatycznie przesuwana do pozycji telefonu (po włączeniu GPS)
- Obracanie mapy zgodnie z kierunkiem ruchu GPS (gdy urządzenie udostępnia wiarygodny bearing)
- Przycisk włączania/wyłączania śledzenia
- Ręczny testowy licznik czasu
- Powiadomienie GPS i zatrzymanie usługi

## Ograniczenia
- To **widok 2D z obrotem**, NIE mapa perspektywiczna 3D jak Google Maps. osmdroid nie obsługuje pochylenia kamery. Do prawdziwego widoku 3D potrzebna będzie migracja do silnika z obsługą pitch/tilt.
- Brak zweryfikowanych punktów OPP w Warszawie i okolicach. Plik opp_segments.json jest pusty.
- Brak automatycznego rozpoznawania OPP, nawigacji po trasie i wskazówek zakręt po zakręcie.
- Na postoju orientacja może być nieznana; obrót działa na podstawie bearing GPS podczas ruchu.
- Nie używaj telefonu podczas prowadzenia. Koniec licznika nie oznacza końca pomiaru ani prawa do przekroczenia limitu.

## Instalacja
Zawartość tego katalogu wgraj do repozytorium GitHub (nie folder nadrzędny), następnie Actions → najnowszy Build → Artifacts → pobierz ZIP z APK. Debug APK może mieć inny podpis niż poprzednia instalacja. Zachowaj kopię danych przed odinstalowaniem starszej aplikacji.

## Uwaga dotycząca projektu
Projekt przygotowano na podstawie archiwum 6.0; jego uruchomienie na Samsungu wymaga kompilacji i testów.
