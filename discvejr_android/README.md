# DiscVejr Android-widget v1.0.0

Native Android-app og startskærmswidget på dansk, Android 8.0+ (API 26+), med Kvaglund i Esbjerg som standard.

## Funktioner

- Widget på 4 × 3 felter, der kan ændres i størrelse
- Open-Meteo vejrdata: temperatur, føles-som, vind, retning, vindstød, regnrisiko i de næste 3 timer og solnedgang
- Discgolf-score 0–100 baseret på vejrforhold, ikke på golfvejr-indekser
- Råd om glow og tordenvejr
- Opdatering manuelt og omtrent hver halve time, når Android tillader det
- Valgfri omtrentlige lokation, ingen konto eller tracking
- Lokal cache med markering, når vejret er over 2 timer gammelt

## Bygning og installation

Projektet er del af [ReneLS365/DiscVejr](https://github.com/ReneLS365/DiscVejr), og GitHub Actions bygger automatisk en debug-signeret APK ved kodeændringer på `main`.

1. [Åbn GitHub Actions](https://github.com/ReneLS365/DiscVejr/actions/workflows/android-apk.yml) og vælg en grøn bygning.
2. Download `DiscVejr-Android-APK`, pak ZIP ud og installer `app-debug.apk`.
3. Åbn appen og tryk **Opdater vejret nu**.
4. Hold på Android-startskærmen → **Widgets** → **DiscVejr**.

Du kan også åbne mappen i Android Studio (JDK 17) og bygge en APK.

## Teknisk

- Java, Android App Widget (RemoteViews), JobScheduler og HTTPS
- `minSdk=26`, `targetSdk=35`, Android Gradle Plugin 8.7.3, Gradle 8.9, Java 17
- Omtrentlige standardkoordinater for Kvaglund: 55.49202, 8.49351
- Lokationsadgang bliver først forespurgt, når brugeren vælger egen position
- Vejret er en prognose, ikke en lokal målestation; score er ingen sikkerhedsgaranti

Test fra denne mappe:

```sh
mkdir -p /tmp/discvejr-tests
javac -d /tmp/discvejr-tests app/src/main/java/dk/discvejr/widget/WeatherLogic.java tests/WeatherLogicTest.java
java -cp /tmp/discvejr-tests dk.discvejr.widget.WeatherLogicTest
```

Vejrdata: [Open-Meteo](https://open-meteo.com/). Ved kommerciel distribution skal deres aktuelle licensvilkår kontrolleres.
