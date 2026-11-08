# OPP TIMER 5.0 — natywna aplikacja Android (projekt źródłowy)

**Status:** projekt rozwojowy, nie gotowy produkt do jazdy. Paczka zawiera kod źródłowy, **nie gotowy APK**.

## Co działa w kodzie
- Natywny ekran Android i mapa OpenStreetMap (osmdroid).
- Pobieranie pozycji GPS przez usługę pierwszoplanową z widocznym powiadomieniem.
- Pokazywanie pozycji i prędkości GPS na mapie.
- Testowy licznik 5 minut dla **fikcyjnego** odcinka 10 km / 120 km/h.
- Podpis © OpenStreetMap contributors pod mapą.

## Czego jeszcze nie ma
- Zweryfikowanej bazy początku/końca OPP, kierunków i limitów.
- Automatycznego wykrywania prawdziwych OPP i niezawodnej obsługi tuneli.
- Wyznaczania tras, zgłoszeń wypadków i korków, głosowych alertów.
- Testów na fizycznym urządzeniu i podpisanej wersji produkcyjnej.

## Budowanie APK wyłącznie z telefonu i GitHuba
**Uwaga:** To oddzielny projekt Android. NIE wgrywaj go do repozytorium `opp-timer` obsługującego GitHub Pages, bo pomieszasz projekt strony z projektem Android.

1. Na GitHubie utwórz nowe publiczne lub prywatne repozytorium `opp-timer-android`.
2. Rozpakuj ZIP i wgraj **zawartość folderu** (nie sam folder) do katalogu głównego nowego repozytorium. Zachowaj strukturę `app/src/main/...` i `.github/workflows/build-apk.yml`.
3. GitHub w przeglądarce mobilnej może nie pozwalać wygodnie wgrać całych folderów. Najłatwiej użyć mobilnego edytora Git z obsługą repozytoriów lub opcji dodania plików z pełną ścieżką. Nie wrzucaj samego ZIP-a: workflow nie rozpakowuje archiwum.
4. Otwórz **Actions → Build Android APK → Run workflow** (albo workflow uruchomi się po push do `main`).
5. Po zielonym wyniku otwórz przebieg workflow → **Artifacts → OPP-TIMER-5-debug-APK**. Pobierz ZIP z APK, rozpakuj, uruchom `app-debug.apk` na Samsungu.
6. Samsung może zapytać o pozwolenie na instalowanie aplikacji z danego źródła. Zezwalaj wyłącznie, jeśli ufasz własnemu kodowi i wynikowi własnego workflow.

**Debug APK** służy do testów, nie do publikacji w Google Play. W razie czerwonego błędu Actions wyślij jego zrzut lub log.

## Bezpieczeństwo
Przestrzegaj znaków drogowych. Czas nie gwarantuje uniknięcia mandatu. Nie obsługuj telefonu podczas prowadzenia. GPS w tle wymaga uprawnień i może być ograniczany przez system.
