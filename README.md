# OPP TIMER 6.0 — Polska (wersja testowa)

Natywna aplikacja Android (Kotlin, osmdroid), do instalacji na Samsungu przez APK z GitHub Actions.

## Obecne funkcje
- mapa OpenStreetMap Polski i śledzenie pozycji po udzieleniu zgody;
- GPS foreground service uruchamiany wyłącznie przyciskiem, powiadomienie z możliwością zatrzymania;
- prędkość GPS (jeśli odbiornik udostępnia wiarygodny pomiar);
- ręczny kalkulator orientacyjnego czasu przejazdu: `ceil(długość_km / limit_km_h * 3600)`;
- testowe odliczanie i polskie komunikaty TTS (jeśli język jest dostępny).

## WAŻNE: nie jest to jeszcze gotowy system OPP
Plik `app/src/main/assets/opp_segments.json` jest **pusty** (`[]`). Aplikacja **nie rozpoznaje** rzeczywistych odcinków OPP, nie ma aktualnej bazy całej Polski, nie wykrywa automatycznie wjazdu/wyjazdu, nie oblicza rzeczywistej średniej prędkości na OPP i nie zapewnia ostrzeżeń o rzeczywistych odcinkach. Nazwa 6.0 oznacza wersję rozwojową, nie potwierdza gotowości produkcyjnej.

Nie obsługuj telefonu podczas jazdy. Upływ obliczonego czasu nie oznacza końca odcinka OPP i nie upoważnia do przekroczenia limitu.

## Budowanie
GitHub Actions → najnowszy `Build OPP TIMER APK` → Artifacts → `OPP-TIMER-6.0-debug` → pobierz ZIP → rozpakuj `app-debug.apk` na Androidzie.

**Uwaga o aktualizacji:** debug APK zbudowane w różnych uruchomieniach GitHub Actions mogą być podpisane różnymi kluczami. Jeśli Android zgłosi konflikt podpisu, wykonaj kopię danych (jeśli potrzebna), odinstaluj starą aplikację i zainstaluj nową. Dla aktualizacji bez utraty danych potrzebny jest stały klucz podpisu.

## Dalsze prace przed wydaniem
1. Pozyskać legalnie używaną, aktualną i zweryfikowaną bazę OPP (współrzędne początku i końca, przebieg, kierunek, limit, data weryfikacji).
2. Wdrożyć wykrywanie wjazdu/wyjazdu, uwzględnić niedokładność GPS i ograniczenia prędkości zależne od pojazdu.
3. Przetestować GPS, foreground service, TTS, wygaszanie ekranu i działanie na urządzeniach Samsung.
4. Dodać trwałe podpisywanie APK, testy automatyczne i politykę prywatności.
